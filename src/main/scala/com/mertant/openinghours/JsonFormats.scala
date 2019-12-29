package com.mertant.openinghours

import com.mertant.openinghours.dto.{OpeningHoursDTO, OpeningTimeDTO}
import com.mertant.openinghours.model.TimeType
import com.mertant.openinghours.model.TimeType.TimeType
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat}

import scala.util.Try

object JsonFormats  {
  import DefaultJsonProtocol._

  implicit object TimeTypeFormat extends JsonFormat[TimeType] {

    def write(obj: TimeType): JsValue = {
      JsString(obj.toString)
    }

    def read(json: JsValue): TimeType = json match {
      case JsString(str) =>
        Try(TimeType.withName(str)).getOrElse(throw new IllegalArgumentException("Not a valid TimeType: " + str))
      case v: JsValue =>
        throw DeserializationException("Malformed TimeType: " + v.toString())
    }
  }

  implicit val openingTimeDTOFormat = jsonFormat2(OpeningTimeDTO)
  implicit val openingHoursDTOFormat = jsonFormat7(OpeningHoursDTO)
}
