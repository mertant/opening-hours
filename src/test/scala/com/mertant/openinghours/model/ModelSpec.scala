package com.mertant.openinghours.model

import java.time.{DateTimeException, LocalTime}

import org.scalatest.{Matchers, WordSpec}

class ModelSpec extends WordSpec with Matchers {
   "TimeOfWeek model" should {
     "be able to create" in {
       noException should be thrownBy TimeOfWeek(0, LocalTime.of(0,0))
       noException should be thrownBy TimeOfWeek(0, LocalTime.of(12,0))
       noException should be thrownBy TimeOfWeek(6, LocalTime.of(12,0))
       noException should be thrownBy TimeOfWeek(6, LocalTime.of(23,0))
     }
     "not be able to create outside range" in {
       an [IllegalArgumentException] should be thrownBy TimeOfWeek(-1, LocalTime.of(12,0))
       an [IllegalArgumentException] should be thrownBy TimeOfWeek(7, LocalTime.of(12,0))
       an [DateTimeException] should be thrownBy TimeOfWeek(0, LocalTime.of(-5,0))
       an [DateTimeException] should be thrownBy TimeOfWeek(0, LocalTime.of(24,0))
     }
   }

   "Interval model" should {
     "be able to create intervals within one day" in {
       noException should be thrownBy Interval(
         TimeOfWeek(0, LocalTime.of(0,0)),
         TimeOfWeek(0, LocalTime.of(12,0)))
       noException should be thrownBy Interval(
         TimeOfWeek(6, LocalTime.of(12,0)),
         TimeOfWeek(6, LocalTime.of(23,0)))
     }

     "be able to create intervals that cross over into the next day" in {
       noException should be thrownBy Interval(
         TimeOfWeek(0, LocalTime.of(18,0)),
         TimeOfWeek(1, LocalTime.of(3,0)))
     }

     "be able to create intervals that cross over from sunday to monday" in {
       noException should be thrownBy Interval(
         TimeOfWeek(6,  LocalTime.of(20,0)),
         TimeOfWeek(0,  LocalTime.of(4,0)))
     }

     "be able to create an end time before the start time on the same day" +
       "(opening hours assumed to span several days, e.g. a 24-hour service station that only closes for a few hours per week)" in {
       noException should be thrownBy Interval(
         TimeOfWeek(0,  LocalTime.of(19,0)),
         TimeOfWeek(0,  LocalTime.of(12,0)))
     }

     "not be able to create an interval with length 0" in {
       an [IllegalArgumentException] should be thrownBy Interval(
         TimeOfWeek(0,  LocalTime.of(12,0)),
         TimeOfWeek(0,  LocalTime.of(12,0)))
     }
   }

   "OpeningHours model" should {
     "be able to create empty" in {
       val intervals = Seq.empty
       val model: OpeningHours = new OpeningHours(intervals)
       model.intervals.size shouldBe 0
     }
     "be able to create with one interval" in {
       val interval = Interval(TimeOfWeek(0,  LocalTime.of(9,0)), TimeOfWeek(0,  LocalTime.of(16,0)))
       val model: OpeningHours = new OpeningHours(Seq(interval))
       model.intervals.size shouldBe 1
     }
     "be able to create with an interval crossing over into the next day" in {
       val interval = Interval(
         TimeOfWeek(0, LocalTime.of(18, 0)),
         TimeOfWeek(0, LocalTime.of(3,0)))
       val model: OpeningHours = new OpeningHours(Seq(interval))
       model.intervals.size shouldBe 1
     }
   }

}
