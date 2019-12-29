package com.mertant.openinghours

import com.mertant.openinghours.model.TimeType.{close, open}
import com.mertant.openinghours.JsonFormats._
import com.mertant.openinghours.dto.{OpeningHoursDTO, OpeningTimeDTO}
import com.mertant.openinghours.model.TimeType
import org.scalatest.{Matchers, WordSpec}
import spray.json.{DeserializationException, JsString, enrichString}

class JsonFormatsSpec extends WordSpec with Matchers {
  import JsonFormatsSpec._

  "OpeningHoursDTO format" should {
    "be able to read empty" in {
      val dto: OpeningHoursDTO = openingHoursDTOFormat.read(emptyJsonString.parseJson)
      dto shouldEqual emptyDto
    }

    "be able to read one opening interval per day" in {
      val dto: OpeningHoursDTO = openingHoursDTOFormat.read(mondayOnlyJsonString.parseJson)
      dto.monday.size shouldBe 2
    }

    "be able to read several opening intervals per day" in {
      val dto: OpeningHoursDTO = openingHoursDTOFormat.read(severalJsonString.parseJson)
      dto.monday.size shouldBe 2
      dto.tuesday.size shouldBe 2
      dto.sunday.size shouldBe 4
    }

    "require all days to have at least an empty array" in {
      an [DeserializationException] should be thrownBy openingHoursDTOFormat.read(deficientJsonString.parseJson)
    }

    "require correct TimeType" in {
      an [IllegalArgumentException] should be thrownBy openingHoursDTOFormat.read(invalidTimeTypeJsonString.parseJson)
    }

    "be able to write" in {
      openingHoursDTOFormat.write(emptyDto).asJsObject shouldEqual emptyJsonString.parseJson
      openingHoursDTOFormat.write(mondayOnlyDto).asJsObject shouldEqual mondayOnlyJsonString.parseJson
      openingHoursDTOFormat.write(severalDto) shouldEqual severalJsonString.parseJson
    }
  }

  "TimeType format" should {
    "be able to read" in {
      TimeTypeFormat.read("\"close\"".parseJson) shouldBe TimeType.close
      TimeTypeFormat.read("\"open\"".parseJson) shouldBe TimeType.open
    }

    "be able to write" in {
      TimeTypeFormat.write(TimeType.close) shouldBe JsString("close")
      TimeTypeFormat.write(TimeType.open) shouldBe JsString("open")
    }

    "throw error with invalid type" in {
      an [IllegalArgumentException] should be thrownBy TimeTypeFormat.read("\"hamburger\"".parseJson)
      an [DeserializationException] should be thrownBy TimeTypeFormat.read("{ \"close\": true }".parseJson)
    }
  }
}

object JsonFormatsSpec {
  val emptyJsonString =
    """{
         "monday" : [],
          "tuesday": [],
          "wednesday": [],
          "thursday": [],
          "friday": [],
          "saturday": [],
          "sunday": []
        }"""

  val mondayOnlyJsonString =
    """{
         "monday" : [
            { "type" : "open",  "value" : 32400 },
            { "type" : "close", "value" : 72000 }
          ],
          "tuesday": [],
          "wednesday": [],
          "thursday": [],
          "friday": [],
          "saturday": [],
          "sunday": []
        }"""


  val severalJsonString =
    """{
          "monday" : [
            { "type" : "open",  "value" : 32400 },
            { "type" : "close", "value" : 72000 }
          ],
          "tuesday": [
            { "type" : "open",  "value" : 32400 },
            { "type" : "close", "value" : 72000 }
          ],
          "wednesday": [],
          "thursday": [],
          "friday": [],
          "saturday": [],
          "sunday": [
            { "type" : "open",  "value" : 32400 },
            { "type" : "close", "value" : 62800 },
            { "type" : "open",  "value" : 70000 },
            { "type" : "close", "value" : 72000 }
          ]
        }"""

  val deficientJsonString =
    """{
        "monday" : [
          { "type" : "open",  "value" : 32400 },
          { "type" : "close", "value" : 72000 }
        ]
      }"""

  val invalidTimeTypeJsonString =
    """{
        "monday" : [
          { "type" : "open",  "value" : 32400 },
          { "type" : "hamburger", "value" : 72000 }
         ],
         "tuesday": [],
         "wednesday": [],
         "thursday": [],
         "friday": [],
         "saturday": [],
         "sunday": []
      }"""

  val emptyDto: OpeningHoursDTO = OpeningHoursDTO(
    monday = Seq.empty,
    tuesday = Seq.empty,
    wednesday = Seq.empty,
    thursday = Seq.empty,
    friday = Seq.empty,
    saturday = Seq.empty,
    sunday = Seq.empty
  )

  val intervals1 = Seq(OpeningTimeDTO(open, 32400), OpeningTimeDTO(close, 72000))
  val mondayOnlyDto: OpeningHoursDTO = emptyDto.copy(monday = intervals1)

  val intervals2 = Seq(
    OpeningTimeDTO(open, 32400), OpeningTimeDTO(close, 62800),
    OpeningTimeDTO(open, 70000), OpeningTimeDTO(close, 72000))
  val severalDto: OpeningHoursDTO = emptyDto.copy(monday = intervals1, tuesday = intervals1, sunday = intervals2)
}