package actors

import actors.BasicPaxosProcessActor._
import actors.Paths.nodesPath
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.cluster.sharding.ShardRegion.{ExtractEntityId, ExtractShardId}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import com.typesafe.scalalogging.StrictLogging
import conf.Config
import utils.SequenceNumber


class BasicPaxosProcessActor extends Actor
  with StrictLogging {

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

  override def receive: Receive = {
    case Create(id) =>
      this.id = id
      context.become(postCreate)
      logger.info(s"Created $id entity")
    case other =>
      logger.warn(s"Got $other in not created state")
  }

  def postCreate: Receive = readWrite orElse {

    case Prepare(_, proposerId, value, seqNumber) =>
      if (seqNumber > highestSeqNumber) {
        highestSeqNumber = seqNumber
        nodesPath ! Promise(proposerId, id, value, seqNumber)
      }

    case Promise(_, promiserId, value, seqNumber) =>
      currentSeqNumber.filter(_ == seqNumber).foreach { curSeqNo =>
        currentPromisers ::= promiserId
        if (currentPromisers.size == consensusValue) {
          currentPromisers.foreach { promiserId =>
            nodesPath ! Accept(promiserId, id, value, seqNumber)
          }
          currentClient.foreach(_ ! WriteSucceeded)
        } else if (currentPromisers.size > consensusValue) {
          nodesPath ! Accept(promiserId, id, value, seqNumber)
        }
      }

    case Accept(_, proposerId, value, seqNumber) =>
      if (seqNumber == highestSeqNumber) {
        data = Some(value)
      }

  }

  def readWrite: Receive = {
    case ReadValue(_, client) =>
      client ! ReadResponse(data)

    case WriteValue(_, client, value) =>
      currentClient = Some(client)
      currentPromisers = List()
      val seqNo = SequenceNumber.generate(id)
      currentSeqNumber = Some(seqNo)
      nodesIds.foreach(nodesPath ! Prepare(_, id, value, seqNo))
  }

}

object BasicPaxosProcessActor {

  val nodesIds = Config.nodesIds

  val typeName = "Process"

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

  case class ReadResponse(value: Option[String])
  case object WriteSucceeded

}
