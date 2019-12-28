package com.mertant.openinghours

import com.mertant.openinghours.dto.{HumanReadableOpeningHours, OpeningHoursDTO, OpeningTimeDTO}
import spray.json.DefaultJsonProtocol

object JsonFormats  {
  import DefaultJsonProtocol._

  implicit val openingTimeDTOFormat = jsonFormat2(OpeningTimeDTO)
  implicit val openingHoursDTOFormat = jsonFormat7(OpeningHoursDTO)
  implicit val humanReadableOpeningHoursFormat = HumanReadableOpeningHours.format
}
