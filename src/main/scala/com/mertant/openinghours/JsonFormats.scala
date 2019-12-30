package com.mertant.openinghours

import com.mertant.openinghours.dto.{OpeningHoursDTO, MomentDTO}
import com.mertant.openinghours.model.TimeType
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat}

import scala.util.Try

object JsonFormats  {
  import DefaultJsonProtocol._

  // It seems that spray-json does not provide an Enumeration parser by default, so we need to create one.
  class EnumFormat[A <: Enumeration](a: A) extends JsonFormat[A#Value] {
    private val enumTypeName = a.getClass.getSimpleName

    def write(obj: A#Value): JsValue = {
      JsString(obj.toString)
    }

    def read(json: JsValue): A#Value = json match {
      case JsString(str) =>
        Try(a.withName(str)).getOrElse(
          throw new IllegalArgumentException(s"Not a valid $enumTypeName value: $str. Valid values are: ${a.values.toSeq.mkString(", ")}"))
      case v: JsValue =>
        throw DeserializationException(s"Malformed $enumTypeName: $v")
    }
  }

  implicit val TimeTypeFormat = new EnumFormat(TimeType)

  implicit val momentDtoFormat = jsonFormat2(MomentDTO)
  implicit val openingHoursDtoFormat = jsonFormat7(OpeningHoursDTO)
}
