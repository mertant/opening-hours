package com.mertant.openinghours.model

import com.mertant.openinghours.dto.{OpeningHoursDTO, MomentDTO}
import java.time.LocalTime

import com.mertant.openinghours.model.TimeType.TimeType
import com.mertant.openinghours.model.WeekDay.WeekDay

case class OpeningHours(intervals: Seq[Interval]) {
  def humanReadableString: String = OpeningHours.toHumanReadableString(this)
}

object OpeningHours {
  def fromDto(dto: OpeningHoursDTO): OpeningHours = {
    val momentsByDay: Seq[Seq[MomentDTO]] = Seq(dto.monday, dto.tuesday, dto.wednesday, dto.thursday, dto.friday, dto.saturday, dto.sunday)
    val intervals: Seq[Interval] = dayOpeningHoursToIntervals(momentsByDay)

    if (hasOverlaps(intervals)) {
      throw new IllegalArgumentException("Opening periods should not overlap.")
    }

    new OpeningHours(intervals)
  }

  private def dayOpeningHoursToIntervals(momentsByDay: Seq[Seq[MomentDTO]]): Seq[Interval] = {
    val momentsWithDays: Seq[(MomentDTO, WeekDay)] = momentsByDay.zipWithIndex.flatMap { case (moment, dayOfWeekIndex) =>
      moment.map((_, WeekDay.of(dayOfWeekIndex)))
    }

    val timesOfWeek: Seq[(TimeOfWeek, TimeType)] = momentsWithDays.map { case (moment, dayOfWeek) =>
      val time = LocalTime.ofSecondOfDay(moment.value)
      (TimeOfWeek(dayOfWeek, time), moment.`type`)
    }

    timesOfWeekToIntervals(timesOfWeek)
  }

  private def timesOfWeekToIntervals(timesOfWeekAndTypes: Seq[(TimeOfWeek, TimeType)]): Seq[Interval] = {
    val openingTimesOnly = timesOfWeekAndTypes.zipWithIndex.filter { case ((_, timeType), _) => timeType == TimeType.open }
    if (openingTimesOnly.size * 2 != timesOfWeekAndTypes.size) {
      throw new IllegalArgumentException("There should be an equal amount of opening and closing times.")
    }

    openingTimesOnly.map { case ((start, _), index) =>
      val nextIndex = (index + 1) % timesOfWeekAndTypes.size // loop back to start if opening time is the last of the week
      val (nextTime, nextType): (TimeOfWeek, TimeType) = timesOfWeekAndTypes(nextIndex)
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
    val weekDaysInOrder = WeekDay.values.toSeq
    val dayStrings = weekDaysInOrder.map(openingHoursStringForDay(openingHours, _))
    dayStrings.mkString(daySeparator)
  }

  private def openingHoursStringForDay(openingHours: OpeningHours, day: WeekDay): String = {
    val intervalsForDay: Seq[Interval] = openingHours.intervals.filter(_.start.weekDay == day)
    val intervalStrings: Seq[String] = openingHourStringsForIntervals(intervalsForDay)
    val dayOpeningHoursString = if (intervalStrings.isEmpty) "Closed" else intervalStrings.mkString(intervalSeparator)
    s"$day: $dayOpeningHoursString"
  }

  private def openingHourStringsForIntervals(intervals: Seq[Interval]): Seq[String] = {
    intervals.map { case Interval(start, end) =>
      val endString = toAmPmString(end.time)
      s"${toAmPmString(start.time)} - $endString"
    }
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
