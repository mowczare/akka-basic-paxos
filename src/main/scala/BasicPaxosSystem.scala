import actors.BasicPaxosActor
import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContextExecutor

object BasicPaxosSystem extends App with LazyLogging {

  implicit val system: ActorSystem = ActorSystem("basic-paxos-system")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val actor1 = system.actorOf(BasicPaxosActor.props)
  val actor2 = system.actorOf(BasicPaxosActor.props)
  val actor3 = system.actorOf(BasicPaxosActor.props)

}