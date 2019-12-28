package com.mertant.openinghours

import com.mertant.openinghours.JsonFormats._
import com.mertant.openinghours.dto.{OpeningHoursDTO, OpeningTimeDTO}
import org.scalatest.{Matchers, WordSpec}
import spray.json._

class JsonFormatsSpec extends WordSpec with Matchers  {
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


  val emptyDto: OpeningHoursDTO = OpeningHoursDTO(
    monday = Seq.empty,
    tuesday = Seq.empty,
    wednesday = Seq.empty,
    thursday = Seq.empty,
    friday = Seq.empty,
    saturday = Seq.empty,
    sunday = Seq.empty
  )

  val intervals1 = Seq(OpeningTimeDTO("open", 32400), OpeningTimeDTO("close", 72000))
  val mondayOnlyDto: OpeningHoursDTO = emptyDto.copy(monday = intervals1)

  val intervals2 = Seq(
    OpeningTimeDTO("open", 32400), OpeningTimeDTO("close", 62800),
    OpeningTimeDTO("open", 70000), OpeningTimeDTO("close", 72000))
  val severalDto: OpeningHoursDTO = emptyDto.copy(monday = intervals1, tuesday = intervals1, sunday = intervals2)

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

    "be able to write" in {
      openingHoursDTOFormat.write(emptyDto).asJsObject shouldEqual emptyJsonString.parseJson
      openingHoursDTOFormat.write(mondayOnlyDto).asJsObject shouldEqual mondayOnlyJsonString.parseJson
      openingHoursDTOFormat.write(severalDto) shouldEqual severalJsonString.parseJson
    }
  }

  "OpeningHours format" should {
    "be able to read" in {

    }
    "be able to write" in {

    }
  }
}
