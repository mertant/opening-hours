package com.mertant.openinghours.model

import java.time.LocalTime

case class TimeOfWeek(weekDay: WeekDay.Value, time: LocalTime) {
  def isDuring(interval: Interval): Boolean = {
    this.isAfter(interval.start) && interval.end.isAfter(this)
  }

  def isAfter(that: TimeOfWeek): Boolean = {
    (this.weekDay == that.weekDay && this.time.isAfter(that.time)) ||
      this.weekDay.id > that.weekDay.id
  }
}

object TimeOfWeek {
  def apply(dayOfWeekIndex: Int, time: LocalTime): TimeOfWeek = {
    val weekDay = WeekDay.of(dayOfWeekIndex)
    new TimeOfWeek(weekDay, time)
  }
}