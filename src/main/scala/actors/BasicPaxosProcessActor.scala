package actors

import actors.BasicPaxosProcessActor._
import actors.Paths.nodesPath
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.cluster.sharding.ShardRegion.{ExtractEntityId, ExtractShardId}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import com.typesafe.scalalogging.{LazyLogging, StrictLogging}
import conf.Config
import utils.SequenceNumber


class BasicPaxosProcessActor extends Actor
  with ActorLogging {

  implicit val system = context.system

  //common paxos process data
  private var id: String = ""
  private var data: Option[String] = None
  private var highestSeqNumber: SequenceNumber = SequenceNumber.min

  //proposer Cache
  private var currentClient: Option[ActorRef] = None
  private var currentPromisers: List[String] = List()
  private var currentSeqNumber: Option[SequenceNumber] = None

  private val consensusValue = (nodesIds.size / 2) + 1

  override def receive: Receive = preCreate

  def preCreate: Receive = {
    case Create(id) =>
      log.info(s"Created $id entity")
      this.id = id
      context.become(postCreate)
    case other =>
      log.warning(s"Got $other in not created state")
  }

  def postCreate: Receive = readWrite orElse {

    case Prepare(_, proposerId, value, seqNumber) =>
      log.info(s"Got Prepare msg from $proposerId with value $value and seqNo: $seqNumber")
      if (seqNumber > highestSeqNumber) {
        log.info(s"seqNo: $seqNumber is greater than highestSeqNo: $highestSeqNumber, sending Promise")
        highestSeqNumber = seqNumber
        nodesPath ! Promise(proposerId, id, value, seqNumber)
      }

    case Promise(_, promiserId, value, seqNumber) =>
      log.info(s"Got Promise from $promiserId")
      currentSeqNumber.filter(_ == seqNumber).foreach { curSeqNo =>
        currentPromisers ::= promiserId
        if (currentPromisers.size == consensusValue) {
          log.info(s"Got majority of promises, sending Accepts from now on")
          currentPromisers.foreach { promiserId =>
            nodesPath ! Accept(promiserId, id, value, seqNumber)
          }
          data = Some(value)
          currentClient.foreach(_ ! WriteSucceeded(value))
        } else if (currentPromisers.size > consensusValue) {
          nodesPath ! Accept(promiserId, id, value, seqNumber)
        }
      }

    case Accept(_, proposerId, value, seqNumber) =>
      log.info(s"Got Accept from $proposerId")
      if (seqNumber == highestSeqNumber) {
        log.info(s"Seq number is correct, updating value to $value")
        data = Some(value)
      }

    case Kill(_) =>
      log.info(s"Node $id is now down")
      context.become(preCreate)

    case other =>
      log.error(s"Got $other in created state")

  }

  def readWrite: Receive = {
    case ReadValue(_, client) =>
      log.info(s"Got Read request to $id, sending $data")
      client ! ReadResponse(data)

    case WriteValue(_, client, value) =>
      currentClient = Some(client)
      currentPromisers = List()
      val seqNo = SequenceNumber.generate(id)
      log.info(s"Got Write request, sending seqNo: $seqNo")
      highestSeqNumber = seqNo
      currentSeqNumber = Some(seqNo)
      nodesIds.foreach(nodesPath ! Prepare(_, id, value, seqNo))
  }

}

object BasicPaxosProcessActor {

  val nodesIds = Config.nodesIds

  val typeName = "processActors"

  def props = Props(new BasicPaxosProcessActor)

  def clusterSharding()(implicit actorSystem: ActorSystem) =
    ClusterSharding(actorSystem).start(
      typeName = typeName,
      entityProps = props,
      settings = ClusterShardingSettings(actorSystem),
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    )

  def extractEntityId: ExtractEntityId = {
    case a: BasicPaxosCommand => (a.id, a)
  }

  def extractShardId: ExtractShardId = {
    case a: BasicPaxosCommand => (a.id.hashCode % Config.numberOfNodes).toString
  }

  sealed trait BasicPaxosCommand {
    def id: String
  }

  case class Create(id: String) extends BasicPaxosCommand
  case class ReadValue(id: String, client: ActorRef) extends BasicPaxosCommand
  case class WriteValue(id: String, client: ActorRef, value: String) extends BasicPaxosCommand
  case class Prepare(id: String, proposerId: String, value: String, seqNumber: SequenceNumber) extends BasicPaxosCommand
  case class Promise(id: String, promiserId: String, value: String, seqNumber: SequenceNumber) extends BasicPaxosCommand
  case class Accept(id: String, proposerId: String, value: String, seqNumber: SequenceNumber) extends BasicPaxosCommand
  case class Kill(id: String) extends BasicPaxosCommand

  case class ReadResponse(value: Option[String])
  case class WriteSucceeded(value: String)

}
