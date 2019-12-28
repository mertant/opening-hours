package com.mertant.openinghours.model

import com.mertant.openinghours.dto.OpeningHoursDTO

case class OpeningHours(intervals: Seq[Interval]) {
  def humanReadableString: String = {
    s"I am a human readable string of ${intervals.size} opening hours"
  }
}

object OpeningHours {
  def fromDto(dto: OpeningHoursDTO): OpeningHours = {
    new OpeningHours(Seq.empty)
  }
}


case class Interval(start: TimeOfWeek, end: TimeOfWeek)

object Interval {
}

case class TimeOfWeek(dayOfWeek: Int, hourOfDay: Int)

object TimeOfWeek {
}

case class WeekDay(value: Int) {
}

object WeekDay {
  val min = 1
  val max = 7

  def apply(value: Int): WeekDay = {
    if (value > max || value < min) {
      val msg = s"Day must be between $min and $max but was $value"
      throw new IllegalArgumentException(msg)
    }
    WeekDay(value)
  }
}

case class Hour(value: Int) {
}

object Hour {
  val min = 0
  val max = 23

  def apply(value: Int): Hour = {
    if (value > max || value < min) {
      val msg = s"Hour must be between $min and $max but was $value"
      throw new IllegalArgumentException(msg)
    }
    Hour(value)
  }
}