package actors

import akka.actor.{Actor, Props}

class BasicPaxosActor extends Actor {

  override def receive = {
    case _ =>
  }

}

object BasicPaxosActor {
  def props = Props(new BasicPaxosActor)

  sealed trait BasicPaxosCommand
  case object PingCommand extends BasicPaxosCommand
}
