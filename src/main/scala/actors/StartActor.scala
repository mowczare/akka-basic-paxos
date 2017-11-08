package actors

import actors.BasicPaxosProcessActor.Create
import akka.actor.{Actor, ActorSystem, PoisonPill, Props}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}
import conf.Config

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by neo on 06.11.17.
  */
class StartActor extends Actor {

  implicit val as: ActorSystem = context.system

  override def preStart = {
    Config.nodesIds.foreach(nodeId => context.system.scheduler.scheduleOnce(10 seconds, self, nodeId))
  }

  override def receive: Receive = {
    case s: String =>
      Paths.nodesPath ! Create(s)
  }
}

object StartActor {

  def clusterSingletonProps(implicit system: ActorSystem): Props =
    ClusterSingletonManager.props(
      singletonProps = Props(new StartActor),
      terminationMessage = PoisonPill,
      settings = ClusterSingletonManagerSettings(system)
    )

}
