import actors.BasicPaxosProcessActor.Create
import actors.{BasicPaxosProcessActor, Paths, TestActor}
import akka.actor.{ActorSystem, Props}
import com.typesafe.scalalogging.LazyLogging
import conf.Config

import scala.concurrent.ExecutionContextExecutor

object BasicPaxosSystem extends App with LazyLogging {

  implicit val system: ActorSystem = ActorSystem(Config.systemName)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val b = BasicPaxosProcessActor.clusterSharding()

  val a = system.actorOf(Props(new TestActor))

}