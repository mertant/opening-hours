package com.mertant.openinghours.model

import com.mertant.openinghours.dto.{OpeningHoursDTO, OpeningTimeDTO}

case class OpeningHours(intervals: Seq[Interval]) {
  def humanReadableString: String = OpeningHours.toHumanReadableString(this)
}

object OpeningHours {
  import com.mertant.openinghours.model.TimeType.TimeType

  def fromDto(dto: OpeningHoursDTO): OpeningHours = {
    val days = Seq(dto.monday, dto.tuesday, dto.wednesday, dto.thursday, dto.friday, dto.saturday, dto.sunday)
    val intervalsWithDays: Seq[(OpeningTimeDTO, Int)] = days.zipWithIndex.flatMap { case (ivals, index) =>
      ivals.map((_, index))
    }

    val timesAndTypes: Seq[(TimeOfWeek, TimeType)] = intervalsWithDays.map { case (ival, index) =>
      val dayOfWeek = index + 1
      val hours = ival.value / 60 / 60
      (new TimeOfWeek(dayOfWeek, hours), TimeType.withName(ival.`type`))
    }

    val openingTimes = timesAndTypes.zipWithIndex.filter { case ((_, timeType), _) => timeType == TimeType.open }
    val intervals: Seq[Interval] = openingTimes.map { case ((start, _), index) =>
      val (nextTime, nextType): (TimeOfWeek, TimeType) = timesAndTypes(index+1)
      if (!nextType.eq(TimeType.close)) {
        throw new IllegalArgumentException("Each opening time should be immediately followed by a closing time")
      }
      Interval(start, nextTime)
    }

    new OpeningHours(intervals)
  }

  private val intervalSeparator = ", "
  private val daySeparator: String = "\n"

  def toHumanReadableString(openingHours: OpeningHours): String = {
    val dayNames = Seq("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val dayStrings = dayNames.zipWithIndex.map { case (dayName, index) =>
      val dayOfWeek: Int = index+1
      val intervalsForDay = openingHours.intervals.filter(_.start.dayOfWeek == dayOfWeek)
      val intervalStrings: Seq[String] = intervalsForDay.map { case Interval(start, end) =>
        val startString = toAmPmString(start.hourOfDay)
        val endString = toAmPmString(end.hourOfDay)
        s"$startString - $endString"
      }
      val intervalsString = if (intervalStrings.isEmpty) "Closed" else intervalStrings.mkString(intervalSeparator)
      s"$dayName: $intervalsString"
    }
    dayStrings.mkString(daySeparator)
  }

  def toAmPmString(hour: Int): String = {
    hour match {
      case 12 =>
        "12 Noon"
      case 0 =>
        "12 Midnight"
      case h if h < 12 =>
        s"$h AM"
      case h =>
        s"${h-12} PM"
    }
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

object TimeType extends Enumeration {
  type TimeType = Value
  val open, close = Value
}