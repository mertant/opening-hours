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


case class Interval private (start: TimeOfWeek, end: TimeOfWeek) {
}

object Interval {
  def apply(start: TimeOfWeek, end: TimeOfWeek): Interval = {
    if (start.equals(end)) {
      throw new IllegalArgumentException(s"Interval cannot start and end at the same moment in time")
    }
    new Interval(start, end)
  }
}

case class TimeOfWeek(dayOfWeek: Int, hourOfDay: Int)

object TimeOfWeek {
  val dayMin = 1
  val dayMax = 7

  val hourMin = 0
  val hourMax = 23

  def apply(dayOfWeek: Int, hourOfDay: Int): TimeOfWeek = {
    if (dayOfWeek > dayMax || dayOfWeek < dayMin) {
      throw new IllegalArgumentException(s"Day must be between $dayMax and $dayMin but was $dayOfWeek")
    }

    if (hourOfDay > hourMax || hourOfDay < hourMin) {
      throw new IllegalArgumentException(s"Hour must be between $hourMin and $hourMax but was $hourOfDay")
    }

    new TimeOfWeek(dayOfWeek, hourOfDay)
  }
}

case class WeekDay(value: Int) {
}

object WeekDay {
  val min = 1
  val max = 7

  def apply(value: Int): WeekDay = {
    if (value > max || value < min) {
      throw new IllegalArgumentException(s"Day must be between $min and $max but was $value")
    }
    new WeekDay(value)
  }
}

case class Hour(value: Int) {
}

object Hour {
  val min = 0
  val max = 23

  def apply(value: Int): Hour = {
    if (value > max || value < min) {
      throw new IllegalArgumentException(s"Hour must be between $min and $max but was $value")
    }
    new Hour(value)
  }
}