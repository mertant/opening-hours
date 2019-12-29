package com.mertant.openinghours.dto

import com.mertant.openinghours.model.TimeType.TimeType

case class OpeningHoursDTO(monday: Seq[OpeningTimeDTO],
                           tuesday: Seq[OpeningTimeDTO],
                           wednesday: Seq[OpeningTimeDTO],
                           thursday: Seq[OpeningTimeDTO],
                           friday: Seq[OpeningTimeDTO],
                           saturday: Seq[OpeningTimeDTO],
                           sunday: Seq[OpeningTimeDTO]) {

}

case class OpeningTimeDTO(`type`: TimeType,
                          value: Int // time of day in seconds
                         ) {

}
