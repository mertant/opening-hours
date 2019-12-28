package com.mertant.openinghours

import com.mertant.openinghours.dto.{HumanReadableOpeningHours, OpeningHoursDTO, OpeningTimeDTO}
import com.mertant.openinghours.model._
import spray.json.DefaultJsonProtocol

object JsonFormats  {
  import DefaultJsonProtocol._

  implicit val openingTimeDTOFormat = jsonFormat2(OpeningTimeDTO)
  implicit val openingHoursDTOFormat = jsonFormat7(OpeningHoursDTO)
  implicit val humanReadableOpeningHoursFormat = HumanReadableOpeningHours.format

  implicit val hourFormat = jsonFormat1(Hour.apply)
  implicit val weekDayFormat = jsonFormat1(WeekDay.apply)
  implicit val timeOfWeekFormat = jsonFormat2(TimeOfWeek.apply)
  implicit val intervalFormat = jsonFormat2(Interval.apply)
  implicit val openingHoursModelFormat = jsonFormat1(OpeningHours.apply)
}
