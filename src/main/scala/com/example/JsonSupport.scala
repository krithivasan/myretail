package com.example

import com.example.repo.{ ProductInfo, ProductPrice }
import com.example.routes.ProductDetails

//#json-support
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val productPriceJsonFormat = jsonFormat2(ProductPrice)
  implicit val productInfoJsonFormat = jsonFormat2(ProductInfo)

  implicit val productDetailsJsonFormat = jsonFormat3(ProductDetails)
}
//#json-support
