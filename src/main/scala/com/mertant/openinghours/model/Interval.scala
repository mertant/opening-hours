package com.mertant.openinghours.model

case class Interval private (start: TimeOfWeek, end: TimeOfWeek)

object Interval {
  def apply(start: TimeOfWeek, end: TimeOfWeek): Interval = {
    if (start.equals(end)) {
      throw new IllegalArgumentException(s"Interval cannot start and end at the same moment in time")
    }
    new Interval(start, end)
  }
}