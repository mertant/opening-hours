package com.mertant.openinghours.routes

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.mertant.openinghours.JsonFormats
import com.mertant.openinghours.dto.{OpeningHoursDTO, OpeningTimeDTO}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

class OpeningHoursRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest {
  lazy val routes = new OpeningHoursRoutes().routes

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

    "return human readable text for empty object" in {
      val dto = emptyDto

      postRequest(dto) ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`text/plain(UTF-8)`)
        entityAs[String] should ===("""I am a human readable string of 0 opening hours""")
      }
    }

    "return human readable text for object with one opening hour interval" in {
      val openingTime: OpeningTimeDTO = OpeningTimeDTO("open", 32400)
      val closingTime: OpeningTimeDTO = OpeningTimeDTO("close", 72000)
      val dto: OpeningHoursDTO = emptyDto.copy(monday = Seq(openingTime))

      postRequest(dto) ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`text/plain(UTF-8)`)
        entityAs[String] should ===("""I am a human readable string of 0 opening hours""")
      }
    }
  }

  private def postRequest(dto: OpeningHoursDTO): HttpRequest = {
    val userEntity = Marshal(dto).to[MessageEntity].futureValue

    Post("/hours").withEntity(userEntity)
  }
}
