package api

import actors.BasicPaxosProcessActor.{ReadResponse, ReadValue, WriteSucceeded, WriteValue}
import actors.Paths
import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import conf.Config

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Random, Success}

class ClientCtrl(implicit as: ActorSystem, ec: ExecutionContext) extends Controller {

  implicit val timeout: Timeout = 5 seconds

  override def endpoints: Route = {
    pathPrefix("paxos") {
      paxosRoute
    }
  }

  private def paxosRoute: Route = {
    pathEnd {
      (post & entity(as[String])) { newValue =>
        val randomNode = Random.shuffle(Config.nodesIds).head
        val response = (Paths.nodesPath ? WriteValue(randomNode, newValue)).mapTo[WriteSucceeded]
        onComplete(response) {
          case Success(succeeded) => complete(StatusCodes.Created -> succeeded.value)
          case Failure(ex) => throw ex
        }
      } ~
        get {
          val result = Config.nodesIds.map { nodeId =>
            (Paths.nodesPath ? ReadValue(nodeId)).mapTo[ReadResponse]
          }
          onComplete(Future.sequence(result)) {
            case Success(succeeded) => complete(StatusCodes.OK -> succeeded.toString)
            case Failure(ex) => throw ex
          }
        }
    }
  }


}
