package com.mertant.openinghours.model

import org.scalatest.{Matchers, WordSpec}

class ModelSpec extends WordSpec with Matchers {
   "TimeOfWeek model" should {
     "be able to create" in {
       noException should be thrownBy TimeOfWeek(1, 0)
       noException should be thrownBy TimeOfWeek(1, 12)
       noException should be thrownBy TimeOfWeek(7, 12)
       noException should be thrownBy TimeOfWeek(7, 23)
     }
     "not be able to create outside range" in {
       an [IllegalArgumentException] should be thrownBy TimeOfWeek(0, 12)
       an [IllegalArgumentException] should be thrownBy TimeOfWeek(8, 12)
       an [IllegalArgumentException] should be thrownBy TimeOfWeek(1, -5)
       an [IllegalArgumentException] should be thrownBy TimeOfWeek(1, 24)
     }
   }

   "Interval model" should {
     "be able to create intervals within one day" in {
       noException should be thrownBy Interval(TimeOfWeek(1, 0), TimeOfWeek(1, 12))
       noException should be thrownBy Interval(TimeOfWeek(7, 12), TimeOfWeek(7, 23))
     }

     "be able to create intervals that cross over into the next day" in {
       noException should be thrownBy Interval(TimeOfWeek(1, 18), TimeOfWeek(2, 3))
     }

     "be able to create intervals that cross over from sunday to monday" in {
       noException should be thrownBy Interval(TimeOfWeek(7, 20), TimeOfWeek(1, 4))
     }

     "be able to create an end time before the start time on the same day" +
       "(opening hours assumed to span several days, e.g. a 24-hour service station that only closes for a few hours per week)" in {
       noException should be thrownBy Interval(TimeOfWeek(1, 19), TimeOfWeek(1, 12))
     }

     "not be able to create an interval with length 0" in {
       an [IllegalArgumentException] should be thrownBy Interval(TimeOfWeek(1, 12), TimeOfWeek(1, 12))
     }
   }

   "OpeningHours model" should {
     "be able to create empty" in {
       val intervals = Seq.empty
       val model: OpeningHours = new OpeningHours(intervals)
       model.intervals.size shouldBe 0
     }
     "be able to create with one interval" in {
       val interval = Interval(TimeOfWeek(1, 9), TimeOfWeek(1, 16))
       val model: OpeningHours = new OpeningHours(Seq(interval))
       model.intervals.size shouldBe 1
     }
     "be able to create with an interval crossing over into the next day" in {
       val interval = Interval(TimeOfWeek(1, 18), TimeOfWeek(2, 3))
       val model: OpeningHours = new OpeningHours(Seq(interval))
       model.intervals.size shouldBe 1
     }
   }

}
