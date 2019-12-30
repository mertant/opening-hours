package com.mertant.openinghours.dto

import com.mertant.openinghours.model.TimeType.TimeType

case class OpeningHoursDTO(monday: Seq[MomentDTO],
                           tuesday: Seq[MomentDTO],
                           wednesday: Seq[MomentDTO],
                           thursday: Seq[MomentDTO],
                           friday: Seq[MomentDTO],
                           saturday: Seq[MomentDTO],
                           sunday: Seq[MomentDTO])

case class MomentDTO(`type`: TimeType,
                     value: Int // time of day in seconds
                         )
