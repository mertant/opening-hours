package com.mertant.openinghours.routes

import java.time.DateTimeException

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, get, pathEnd, pathPrefix, post}
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import com.mertant.openinghours.JsonFormats
import com.mertant.openinghours.dto.OpeningHoursDTO
import com.mertant.openinghours.model.OpeningHours

class OpeningHoursRoutes {
  import JsonFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  def getHumanReadableOpeningHours(openingHoursDTO: OpeningHoursDTO): String = {
    val model: OpeningHours = OpeningHours.fromDto(openingHoursDTO)
    model.humanReadableString
  }

  val routes: Route = pathPrefix("hours") {
    pathEnd {
      concat(
        get {
          complete(StatusCodes.OK, "Hello world!")
        },
        post {
          entity(as[OpeningHoursDTO]) { openingHoursDTO =>
            val result: String = getHumanReadableOpeningHours(openingHoursDTO)
            complete(StatusCodes.OK, result)
          }
        })
    }
  }

  implicit def openingHoursExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case e: IllegalArgumentException =>
        complete(StatusCodes.BadRequest, e)
      case e: DateTimeException =>
          complete(StatusCodes.BadRequest, e)
      case e: Exception =>
        complete(StatusCodes.InternalServerError, e)
    }

}
