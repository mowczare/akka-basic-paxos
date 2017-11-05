package actors

import actors.BasicPaxosProcessActor._
import akka.actor.{Actor, ActorRef, Props}
import com.typesafe.scalalogging.StrictLogging
import utils.SequenceNumber

class BasicPaxosProcessActor(client: ActorRef, nodesPath: ActorRef, nodesIds: List[String]) extends Actor
  with StrictLogging {

  //common paxos process data
  private var id: String = ""
  private var data: Option[String] = None
  private var highestSeqNumber: SequenceNumber = SequenceNumber.min

  //proposer Cache
  private var currentPromisers: List[String] = List()
  private var currentSeqNumber: Option[SequenceNumber] = None

  private val consensusValue = (nodesIds.size / 2) + 1

  override def receive: Receive = {
    case Create(id) =>
      this.id = id
      context.become(postCreate)
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
          client ! WriteSucceeded
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
    case ReadValue =>
      client ! ReadResponse(data)

    case WriteValue(_, value) =>
      currentPromisers = List()
      val seqNo = SequenceNumber.generate(id)
      currentSeqNumber = Some(seqNo)
      nodesIds.foreach(nodesPath ! Prepare(_, id, value, seqNo))
  }

}

object BasicPaxosProcessActor {

  def props(client: ActorRef, nodesPath: ActorRef, nodesIds: List[String]) =
    Props(new BasicPaxosProcessActor(client, nodesPath, nodesIds))

  sealed trait BasicPaxosCommand
  case class Create(id: String) extends BasicPaxosCommand
  case class ReadValue(id: String) extends BasicPaxosCommand
  case class WriteValue(id: String, value: String) extends BasicPaxosCommand
  case class Prepare(id: String, proposerId: String, value: String, seqNumber: SequenceNumber) extends BasicPaxosCommand
  case class Promise(id: String, promiserId: String, value: String, seqNumber: SequenceNumber) extends BasicPaxosCommand
  case class Accept(id: String, proposerId: String, value: String, seqNumber: SequenceNumber) extends BasicPaxosCommand
  case class Accepted(id: String, value: String, seqNumber: SequenceNumber) extends BasicPaxosCommand

  case class ReadResponse(value: Option[String])
  case object WriteSucceeded

}
