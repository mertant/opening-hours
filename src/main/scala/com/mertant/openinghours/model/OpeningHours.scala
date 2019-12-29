package com.mertant.openinghours.model

import com.mertant.openinghours.dto.{OpeningHoursDTO, OpeningTimeDTO}
import java.time.LocalTime

case class OpeningHours(intervals: Seq[Interval]) {
  def humanReadableString: String = OpeningHours.toHumanReadableString(this)
}

object OpeningHours {
  import com.mertant.openinghours.model.TimeType.TimeType

  def fromDto(dto: OpeningHoursDTO): OpeningHours = {
    val days: Seq[Seq[OpeningTimeDTO]] = Seq(dto.monday, dto.tuesday, dto.wednesday, dto.thursday, dto.friday, dto.saturday, dto.sunday)
    val intervals: Seq[Interval] = dayOpeningHoursToIntervals(days)

    if (hasOverlaps(intervals)) {
      throw new IllegalArgumentException("Opening periods should not overlap")
    }

    new OpeningHours(intervals)
  }

  private def dayOpeningHoursToIntervals(days: Seq[Seq[OpeningTimeDTO]]): Seq[Interval] = {
    val intervalsWithDays: Seq[(OpeningTimeDTO, Int)] = days.zipWithIndex.flatMap { case (ivals, index) =>
      ivals.map((_, index))
    }

    val timesAndTypes: Seq[(TimeOfWeek, TimeType)] = intervalsWithDays.map { case (ival, dayOfWeek) =>
      val time = LocalTime.ofSecondOfDay(ival.value)
      (TimeOfWeek(dayOfWeek, time), ival.`type`)
    }

    val openingTimes = timesAndTypes.zipWithIndex.filter { case ((_, timeType), _) => timeType == TimeType.open }
    if (openingTimes.size * 2 != timesAndTypes.size) {
      throw new IllegalArgumentException("There should be an equal amount of opening and closing times")
    }

    openingTimes.map { case ((start, _), index) =>
      val (nextTime, nextType): (TimeOfWeek, TimeType) = timesAndTypes(index + 1)
      if (!nextType.eq(TimeType.close)) {
        throw new IllegalArgumentException("Each opening time should be immediately followed by a closing time")
      }
      Interval(start, nextTime)
    }
  }

  private def hasOverlaps(intervals: Seq[Interval]): Boolean = {
    intervals.sliding(2).exists { seqOf2 =>
      val (first, second) =
        if (seqOf2.size < 2) {
          (seqOf2(0), intervals.head) // if the last element, compare it against the first one of the week
        } else {
          (seqOf2(0), seqOf2(1))
        }
      val isDuplicate = intervals.size > 1 && first == second
      isDuplicate || second.start.isDuring(first) || first.end.isDuring(second)
    }
  }

  private val intervalSeparator = ", "
  private val daySeparator: String = "\n"
  private val dayNames = Seq("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

  def toHumanReadableString(openingHours: OpeningHours): String = {
    val dayStrings = dayNames.zipWithIndex.map { case (dayName, dayOfWeek) =>
      val intervalsForDay = openingHours.intervals.filter(_.start.dayOfWeek == dayOfWeek)
      val intervalStrings: Seq[String] = intervalsForDay.map { case Interval(start, end) =>
        val startString = toAmPmString(start.time)
        val endString = toAmPmString(end.time)
        s"$startString - $endString"
      }
      val intervalsString = if (intervalStrings.isEmpty) "Closed" else intervalStrings.mkString(intervalSeparator)
      s"$dayName: $intervalsString"
    }
    dayStrings.mkString(daySeparator)
  }

  def toAmPmString(time: LocalTime): String = {
    val hour: Int = time.getHour
    val minute: Int = time.getMinute
    // ignore seconds

    (hour,minute) match {
      case (12,0) =>
        "12 Noon"
      case (0,0) =>
        "12 Midnight"
      case (h,0) if h < 12 =>
        s"$h AM"
      case (h,m) if h < 12 =>
        val mm = (if (minute > 10) "0" else "") + m
        s"${h-12}:${mm} PM"
      case (h,0) =>
        s"${h-12} PM"
      case (h,m) =>
        val mm = (if (minute > 10) "0" else "") + m
        s"${h-12}:${mm} PM"
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

case class TimeOfWeek(dayOfWeek: Int, time: LocalTime) {
  def isDuring(interval: Interval): Boolean = {
    this.isAfter(interval.start) && interval.end.isAfter(this)
  }

  def isAfter(that: TimeOfWeek): Boolean = {
    (this.dayOfWeek == that.dayOfWeek && this.time.isAfter(that.time)) ||
      this.dayOfWeek > that.dayOfWeek
  }
}

object TimeOfWeek {
  val dayMin = 0
  val dayMax = 6

  val hourMin = 0
  val hourMax = 23

  def apply(dayOfWeek: Int, time: LocalTime): TimeOfWeek = {
    if (dayOfWeek > dayMax || dayOfWeek < dayMin) {
      throw new IllegalArgumentException(s"Day must be between $dayMax and $dayMin but was $dayOfWeek")
    }

    new TimeOfWeek(dayOfWeek, time)
  }
}

case class WeekDay(value: Int) {
}

object WeekDay {
  val min = 0
  val max = 6

  def apply(value: Int): WeekDay = {
    if (value > max || value < min) {
      throw new IllegalArgumentException(s"Day must be between $min and $max but was $value")
    }
    new WeekDay(value)
  }
}

object TimeType extends Enumeration {
  type TimeType = Value
  val open, close = Value
}