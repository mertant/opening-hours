package com.mertant.openinghours.routes

import com.mertant.openinghours.model.TimeType.{close, open}
import com.mertant.openinghours.JsonFormats
import com.mertant.openinghours.dto.{OpeningHoursDTO, MomentDTO}
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

class OpeningHoursRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest {
  lazy val routes = new OpeningHoursRoutes(system).routes

  import JsonFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  val emptyDto: OpeningHoursDTO = OpeningHoursDTO(
    monday = Seq.empty,
    tuesday = Seq.empty,
    wednesday = Seq.empty,
    thursday = Seq.empty,
    friday = Seq.empty,
    saturday = Seq.empty,
    sunday = Seq.empty
  )

  "OpeningHoursRoutes" should {
    "return OK for GET" in {
      // note that there's no need for the host part in the uri:
      val request = HttpRequest(uri = "/hours")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
      }
    }

    "return text for empty object" in {
      val dto = emptyDto

      postRequest(dto) ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`text/plain(UTF-8)`)
        val expected =
          """Monday: Closed
            |Tuesday: Closed
            |Wednesday: Closed
            |Thursday: Closed
            |Friday: Closed
            |Saturday: Closed
            |Sunday: Closed""".stripMargin
        entityAs[String] should ===(expected)
      }
    }

    "return text for object with one opening" in {
      val openingTime: MomentDTO = MomentDTO(open,  32400)
      val closingTime: MomentDTO = MomentDTO(close, 72000)
      val dto: OpeningHoursDTO = emptyDto.copy(monday = Seq(openingTime, closingTime))

      postRequest(dto) ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`text/plain(UTF-8)`)
        val expected =
          """Monday: 9 AM - 8 PM
            |Tuesday: Closed
            |Wednesday: Closed
            |Thursday: Closed
            |Friday: Closed
            |Saturday: Closed
            |Sunday: Closed""".stripMargin
        entityAs[String] should ===(expected)
      }
    }

    "return text for object with several openings" in {
      val openingTime1: MomentDTO = MomentDTO(open,  32400)
      val closingTime: MomentDTO = MomentDTO(close, 72000)
      val times1 = Seq(openingTime1, closingTime)
      val openingTime2: MomentDTO = MomentDTO(open,  43200)
      val times2 = Seq(openingTime2, closingTime)
      val dto: OpeningHoursDTO = emptyDto.copy(monday = times1, tuesday = times1, sunday = times2)

      postRequest(dto) ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`text/plain(UTF-8)`)
        val expected =
          """Monday: 9 AM - 8 PM
            |Tuesday: 9 AM - 8 PM
            |Wednesday: Closed
            |Thursday: Closed
            |Friday: Closed
            |Saturday: Closed
            |Sunday: 12 PM - 8 PM""".stripMargin
        entityAs[String] should ===(expected)
      }
    }

    "return text for object with an opening that crosses over into the next day" in {
      val openingTime: MomentDTO = MomentDTO(open,  72000)
      val closingTime: MomentDTO = MomentDTO(close, 7200)
      val dto: OpeningHoursDTO = emptyDto.copy(monday = Seq(openingTime), tuesday = Seq(closingTime))

      postRequest(dto) ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`text/plain(UTF-8)`)
        val expected =
          """Monday: 8 PM - 2 AM
            |Tuesday: Closed
            |Wednesday: Closed
            |Thursday: Closed
            |Friday: Closed
            |Saturday: Closed
            |Sunday: Closed""".stripMargin
        entityAs[String] should ===(expected)
      }
    }

    "return text for object with an opening that crosses over into the next week (Sun-Mon)" in {
      val openingTime: MomentDTO = MomentDTO(open,  72000)
      val closingTime: MomentDTO = MomentDTO(close, 7200)
      val dto: OpeningHoursDTO = emptyDto.copy(sunday = Seq(openingTime), monday = Seq(closingTime))

      postRequest(dto) ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`text/plain(UTF-8)`)
        val expected =
          """Monday: Closed
            |Tuesday: Closed
            |Wednesday: Closed
            |Thursday: Closed
            |Friday: Closed
            |Saturday: Closed
            |Sunday: 8 PM - 2 AM""".stripMargin
        entityAs[String] should ===(expected)
      }
    }

    "fail when more opening time is the same as the closing time" in {
      val openingTime: MomentDTO = MomentDTO(open,  72000)
      val closingTime: MomentDTO = MomentDTO(close, 72000)
      val dto: OpeningHoursDTO = emptyDto.copy(monday = Seq(openingTime, closingTime))

      postRequest(dto) ~> routes ~> check {
        status should ===(StatusCodes.BadRequest)
      }
    }

    "fail when more opening hours than closing hours" in {
      val openingTime: MomentDTO = MomentDTO(open,  32400)
      val closingTime: MomentDTO = MomentDTO(close, 72000)
      val dto: OpeningHoursDTO = emptyDto.copy(monday = Seq(openingTime), tuesday = Seq(openingTime, closingTime))

      postRequest(dto) ~> routes ~> check {
        status should ===(StatusCodes.BadRequest)
      }
    }

    "fail when more closing hours than opening hours" in {
      val openingTime: MomentDTO = MomentDTO(open,  32400)
      val closingTime: MomentDTO = MomentDTO(close, 72000)
      val dto: OpeningHoursDTO = emptyDto.copy(monday = Seq(closingTime), tuesday = Seq(openingTime, closingTime))

      postRequest(dto) ~> routes ~> check {
        status should ===(StatusCodes.BadRequest)
      }
    }

    "fail when intervals overlap (input alternates open and close)" in {
      val openingTime1: MomentDTO = MomentDTO(open,  32400)
      val closingTime1: MomentDTO = MomentDTO(close, 72000)
      val openingTime2: MomentDTO = MomentDTO(open,  64000)
      val closingTime2: MomentDTO = MomentDTO(close, 79200)
      val dto: OpeningHoursDTO = emptyDto.copy(monday = Seq(openingTime1, closingTime1, openingTime2, closingTime2))

      postRequest(dto) ~> routes ~> check {
        status should ===(StatusCodes.BadRequest)
      }
    }

    "fail when intervals overlap (input in chronological order)" in {
      val openingTime1: MomentDTO = MomentDTO(open,  32400)
      val closingTime1: MomentDTO = MomentDTO(close, 72000)
      val openingTime2: MomentDTO = MomentDTO(open,  64000)
      val closingTime2: MomentDTO = MomentDTO(close, 79200)
      val dto: OpeningHoursDTO = emptyDto.copy(monday = Seq(openingTime1, openingTime2, closingTime1, closingTime2))

      postRequest(dto) ~> routes ~> check {
        status should ===(StatusCodes.BadRequest)
      }
    }

    "fail when intervals are duplicated" in {
      val openingTime: MomentDTO = MomentDTO(open,  32400)
      val closingTime: MomentDTO = MomentDTO(close, 72000)
      val dto: OpeningHoursDTO = emptyDto.copy(monday = Seq(openingTime, closingTime, openingTime, closingTime))

      postRequest(dto) ~> routes ~> check {
        status should ===(StatusCodes.BadRequest)
      }
    }

    "fail when secondOfDay is above max value (86399 = 11.59:59 PM)" in {
      val openingTime: MomentDTO = MomentDTO(open,  32400)
      val closingTime: MomentDTO = MomentDTO(close, 86400)
      val dto: OpeningHoursDTO = emptyDto.copy(monday = Seq(openingTime, closingTime))

      postRequest(dto) ~> routes ~> check {
        status should ===(StatusCodes.BadRequest)
      }
    }
  }

  private def postRequest(dto: OpeningHoursDTO): HttpRequest = {
    val userEntity = Marshal(dto).to[MessageEntity].futureValue

    Post("/hours").withEntity(userEntity)
  }
}
