package api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, ExceptionHandler, Route}
import com.typesafe.scalalogging.StrictLogging

trait Controller extends Directives with StrictLogging {

  val handler = ExceptionHandler {
    case other: Throwable =>
      extractRequest { _ =>
        complete(StatusCodes.InternalServerError -> other.getMessage)
      }
  }

  def routes: Route =
    handleExceptions(handler) {
      endpoints
    }

  def endpoints: Route

}