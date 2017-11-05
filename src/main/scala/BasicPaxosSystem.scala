import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import conf.Config

import scala.concurrent.ExecutionContextExecutor

object BasicPaxosSystem extends App with LazyLogging {

  implicit val system: ActorSystem = ActorSystem(Config.systemName)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

}