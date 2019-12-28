package com.mertant.openinghours.dto

import com.mertant.openinghours.model.OpeningHours
import spray.json.DefaultJsonProtocol
import spray.json.DefaultJsonProtocol.jsonFormat1

case class HumanReadableOpeningHours(text: String) {
}

object HumanReadableOpeningHours {
  import DefaultJsonProtocol._
  val format = jsonFormat1(apply)

  def fromModel(openingHours: OpeningHours): HumanReadableOpeningHours = {
    HumanReadableOpeningHours("empty text")
  }
}