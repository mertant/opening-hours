package com.mertant.openinghours.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, get, pathPrefix, pathEnd, post}
import akka.http.scaladsl.server.Route
import com.mertant.openinghours.JsonFormats
import com.mertant.openinghours.dto.{HumanReadableOpeningHours, OpeningHoursDTO}
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
          complete(StatusCodes.OK)
        },
        post {
          entity(as[OpeningHoursDTO]) { openingHoursDTO =>
            val result: String = getHumanReadableOpeningHours(openingHoursDTO)
            complete(StatusCodes.OK, result)
          }
        })
    }
  }
}
