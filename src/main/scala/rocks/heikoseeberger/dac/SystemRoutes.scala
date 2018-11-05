package rocks.heikoseeberger.dac

import java.nio.file.{Files, Paths}

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import org.apache.logging.log4j.scala.Logging

class SystemRoutes extends Directives with Logging {

  def route: Route =
    path("readiness") {
      get {
        complete(processReadiness)
      }
    } ~
    path("liveness") {
      get {
        complete(HttpResponse(StatusCodes.OK, entity = "Alive and Kickin"))
      }
    }

  private val shutdownIndicatorFilePath = Paths.get("/tmp/shutdown")

  private def processReadiness: HttpResponse =
    if(Files.exists(shutdownIndicatorFilePath)) {
      logger.info(s"Shutdown indicating file $shutdownIndicatorFilePath exists - Shutdown in progress...")
      HttpResponse(StatusCodes.ServiceUnavailable, entity = "Shutdown in progress!!")
    } else {
      HttpResponse(StatusCodes.OK, entity = "Application startup completed successfully!")
    }
}
