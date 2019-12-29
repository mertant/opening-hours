package com.mertant.openinghours.model

import com.mertant.openinghours.dto.{OpeningHoursDTO, OpeningTimeDTO}
import java.time.LocalTime
import com.mertant.openinghours.model.TimeType.TimeType

case class OpeningHours(intervals: Seq[Interval]) {
  def humanReadableString: String = OpeningHours.toHumanReadableString(this)
}

object OpeningHours {
  def fromDto(dto: OpeningHoursDTO): OpeningHours = {
    val days: Seq[Seq[OpeningTimeDTO]] = Seq(dto.monday, dto.tuesday, dto.wednesday, dto.thursday, dto.friday, dto.saturday, dto.sunday)
    val intervals: Seq[Interval] = dayOpeningHoursToIntervals(days)

    if (hasOverlaps(intervals)) {
      throw new IllegalArgumentException("Opening periods should not overlap.")
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
      throw new IllegalArgumentException("There should be an equal amount of opening and closing times.")
    }

    openingTimes.map { case ((start, _), index) =>
      val (nextTime, nextType): (TimeOfWeek, TimeType) = timesAndTypes(index + 1)
      if (!nextType.eq(TimeType.close)) {
        throw new IllegalArgumentException("Each opening time should be immediately followed by a closing time.")
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

    val hourPart = if (hour > 12) hour-12 else hour

    val minutePart = minute match {
      case 0 =>
        ""
      case m if m < 10 =>
        s":0$m"
      case m =>
        s":$m"
    }

    val suffix = if (hour < 12) "AM" else "PM"

    s"$hourPart$minutePart $suffix"
  }
}
