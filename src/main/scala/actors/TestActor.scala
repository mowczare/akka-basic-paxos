package actors

import actors.BasicPaxosProcessActor.Create
import akka.actor.Actor
import conf.Config

import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by neo on 06.11.17.
  */
class TestActor extends Actor {
  implicit val as = context.system

  override def preStart = {
    Config.nodesIds.foreach(nodeId => as.scheduler.scheduleOnce(10 seconds, self, nodeId))
  }

  override def receive: Receive = {
    case s: String =>
      println(s"sending $s")
      Paths.nodesPath ! Create(s)
  }
}
