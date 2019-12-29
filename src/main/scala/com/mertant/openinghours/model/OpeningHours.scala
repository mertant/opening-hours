package com.mertant.openinghours.model

import com.mertant.openinghours.dto.{OpeningHoursDTO, OpeningTimeDTO}
import java.time.LocalTime
import scala.util.Try

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

  def toHumanReadableString(openingHours: OpeningHours): String = {
    val dayStrings = WeekDay.values.toSeq.map { weekDay: WeekDay.Value =>
      val intervalsForDay: Seq[Interval] = openingHours.intervals.filter(_.start.weekDay == weekDay)
      val intervalStrings: Seq[String] = intervalsToStrings(intervalsForDay)
      val intervalsString = if (intervalStrings.isEmpty) "Closed" else intervalStrings.mkString(intervalSeparator)
      s"$weekDay: $intervalsString"
    }
    dayStrings.mkString(daySeparator)
  }

  private def intervalsToStrings(intervals: Seq[Interval]): Seq[String] = {
    val intervalStrings: Seq[String] = intervals.map { case Interval(start, end) =>
      val endString = toAmPmString(end.time)
      s"${toAmPmString(start.time)} - $endString"
    }
    intervalStrings
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

object WeekDay extends Enumeration {
  type WeekDay = Value
  val monday    = Value(0, "Monday")
  val tuesday   = Value(1, "Tuesday")
  val wednesday = Value(2, "Wednesday")
  val thursday  = Value(3, "Thursday")
  val friday    = Value(4, "Friday")
  val saturday  = Value(5, "Saturday")
  val sunday    = Value(6, "Sunday")

  def of(value: Int): Value = {
    Try(WeekDay.apply(value)).getOrElse(
      throw new IllegalArgumentException(s"Day must be between 0 and ${this.values.size-1} but was $value)"))
  }
}

object TimeType extends Enumeration {
  type TimeType = Value
  val open, close = Value
}