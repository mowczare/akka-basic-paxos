import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContextExecutor

object BasicPaxosSystem extends App with LazyLogging {

  implicit val system: ActorSystem = ActorSystem("basic-paxos-system")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

}