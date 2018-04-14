package com.myretail.swagger

import akka.http.scaladsl.server.Directives

trait Site extends Directives {
  val site =
    pathPrefix("myRetail") {
      path("swagger") { getFromResource("swagger/index.html") } ~ getFromResourceDirectory("swagger")
    }
}
