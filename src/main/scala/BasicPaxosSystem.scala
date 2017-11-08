import actors.{BasicPaxosProcessActor, StartActor}
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import api.ClientCtrl
import com.typesafe.scalalogging.StrictLogging
import conf.Config

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object BasicPaxosSystem extends App with StrictLogging {

  implicit val system: ActorSystem = ActorSystem(Config.systemName)
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val paxosProcessesShards = BasicPaxosProcessActor.clusterSharding()

  system.actorOf(StartActor.clusterSingletonProps)

  val binding = Http().bindAndHandle(new ClientCtrl().endpoints, Config.apiHost, Config.apiPort)

  binding onComplete {
    case Success(serverBinding) =>
      logger.info("Successful bind to address {}", serverBinding.localAddress)
    case Failure(throwable) =>
      logger.error("Binding failed", throwable)
      system.terminate()
  }

}