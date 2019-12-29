package com.mertant.openinghours.routes

import java.time.DateTimeException

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, get, handleExceptions, pathEnd, pathPrefix, post}
import akka.http.scaladsl.server.{ExceptionHandler, Route, StandardRoute}
import com.mertant.openinghours.{JsonFormats, Logging}
import com.mertant.openinghours.dto.OpeningHoursDTO
import com.mertant.openinghours.model.OpeningHours

class OpeningHoursRoutes(val system: ActorSystem) extends Logging {
  import JsonFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  def getHumanReadableOpeningHours(openingHoursDTO: OpeningHoursDTO): String = {
    val model: OpeningHours = OpeningHours.fromDto(openingHoursDTO)
    model.humanReadableString
  }

  val routes: Route =
    pathPrefix("hours") {
      handleExceptions(openingHoursExceptionHandler) {
        log.info("/hours called")
        pathEnd {
          concat(
            get {
              logAndRespond("Hello world!")
            },
            post {
              entity(as[OpeningHoursDTO]) { openingHoursDTO =>
                val result: String = getHumanReadableOpeningHours(openingHoursDTO)
                logAndRespond(result)
              }
            })
        }
      }
    }

  private def logAndRespond(responseString: String): StandardRoute = {
    logAndRespond(responseString, StatusCodes.OK)
  }
  private def logAndRespond(responseString: String, status: StatusCode): StandardRoute = {
    log.info("/hours call resulted in success: " + responseString)
    complete(status, responseString)
  }

  private def logAndRespond(t: Throwable): StandardRoute = {
    logAndRespond(t, StatusCodes.InternalServerError)
  }
  private def logAndRespond(t: Throwable, status: StatusCode): StandardRoute = {
    log.error("/hours call resulted in exception: " + t)
    complete(status, t.toString)
  }

  implicit def openingHoursExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case e: IllegalArgumentException =>
        logAndRespond(e, StatusCodes.BadRequest)
      case e: DateTimeException =>
        logAndRespond(e, StatusCodes.BadRequest)
      case t: Throwable =>
        logAndRespond(t)
    }

}
