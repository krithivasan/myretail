package com.example.swagger

import akka.http.scaladsl.server.Directives

trait Site extends Directives {
  val site =
    pathPrefix("mRetail") {
      path("swagger") { getFromResource("swagger/index.html") } ~ getFromResourceDirectory("swagger")
    }
}
