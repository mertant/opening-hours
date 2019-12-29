package com.mertant.openinghours.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.{concat, pathPrefix}
import akka.http.scaladsl.server.Route

object ApplicationRoutes {
    def createRoutes(system: ActorSystem): Route = {
      val hoursRoute: OpeningHoursRoutes = new OpeningHoursRoutes(system)

      concat({
        hoursRoute.routes
      })

    }
}
