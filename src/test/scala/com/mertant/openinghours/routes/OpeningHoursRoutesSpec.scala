package com.mertant.openinghours.routes

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.mertant.openinghours.JsonFormats
import com.mertant.openinghours.dto.OpeningHoursDTO
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

class OpeningHoursRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest {
  lazy val routes = new OpeningHoursRoutes().routes

  import JsonFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  "OpeningHoursRoutes" should {
    "return OK for GET" in {
      // note that there's no need for the host part in the uri:
      val request = HttpRequest(uri = "/hours")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
      }
    }

    "return human readable hours for POST" in {
      val dto = OpeningHoursDTO()
      val userEntity = Marshal(dto).to[MessageEntity].futureValue

      val request = Post("/hours").withEntity(userEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`text/plain(UTF-8)`)
        entityAs[String] should ===("""I am a human readable string of 0 opening hours""")
      }
    }
  }
}
