package com.mertant.openinghours.routes

import akka.http.scaladsl.server.Directives.{concat, pathPrefix}
import akka.http.scaladsl.server.Route

object ApplicationRoutes {
    val hoursRoute: OpeningHoursRoutes = new OpeningHoursRoutes()

    val allRoutes: Route = {
     concat({
       hoursRoute.routes
     })
    }
}
