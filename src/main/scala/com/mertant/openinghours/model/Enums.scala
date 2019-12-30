package com.mertant.openinghours.model

import scala.util.Try

object WeekDay extends Enumeration {
  type WeekDay = Value
  val monday    = Value(0, "Monday")
  val tuesday   = Value(1, "Tuesday")
  val wednesday = Value(2, "Wednesday")
  val thursday  = Value(3, "Thursday")
  val friday    = Value(4, "Friday")
  val saturday  = Value(5, "Saturday")
  val sunday    = Value(6, "Sunday")

  // same as apply, but with more informative error message (instead of Enumeration's default NoSuchElementException)
  def of(value: Int): Value = {
    Try(WeekDay.apply(value)).getOrElse(
      throw new IllegalArgumentException(s"Day must be between 0 and ${this.values.size-1} but was $value)"))
  }
}

object TimeType extends Enumeration {
  type TimeType = Value
  val open, close = Value
}