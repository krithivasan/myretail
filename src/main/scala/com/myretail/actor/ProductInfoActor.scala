package com.myretail.actor

//#product-info-actor
import akka.actor.Status.{ Failure, Success }
import akka.actor.{ Actor, ActorLogging, Props }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse, StatusCodes }
import com.myretail.repo.{ ProductInfo, ProductRepo }
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akka.util.ByteString
import play.api.libs.json.{ JsValue, Json }

import scala.concurrent.Future

object ProductInfoActor {
  final case class InsertProduct(info: ProductInfo)
  final case class ProductInserted(id: Int)
  final case class GetProductPrice(id: Int)
  final case class GetProductName(id: Int)
  final case class UpdatePrice(info: ProductInfo)
  final case class ProductUpdated(id: Int)

  def props(productRepo: ProductRepo): Props = Props(new ProductInfoActor(productRepo))
}

class ProductInfoActor(productRepo: ProductRepo) extends Actor with ActorLogging {
  import ProductInfoActor._
  import context.dispatcher

  val http = Http(context.system)
  implicit val materializer = ActorMaterializer()

  def receive: Receive = {
    case InsertProduct(info) =>
      productRepo.insert(info).map(_ => ProductInserted(info.id)) pipeTo sender
    case GetProductName(id) =>
      val response: Future[HttpResponse] = http.singleRequest(HttpRequest(uri = s"https://redsky.target.com/v2/pdp/tcin/$id?excludes=taxonomy,price,promotion,bulk_ship,rating_and_review_reviews,rating_and_review_statistics,question_answer_statistics"))
      response.flatMap {
        case HttpResponse(StatusCodes.OK, headers, entity, _) =>
          val response: Future[String] = entity.dataBytes.runFold(ByteString(""))(_ ++ _).map { body => body.utf8String
          }
          response.map { r =>
            val json = Json.parse(r)
            val name: String = (json \ "product" \ "item" \ "product_description" \ "title").as[String]
            log.info(s"Product name is $name")
            name
          }
        case resp @ HttpResponse(code, _, _, _) =>
          log.info("Request failed, response code: " + code)
          resp.discardEntityBytes()
          Future.failed(new RuntimeException("Service API returned error"))
      } pipeTo sender
    case GetProductPrice(id) =>
      productRepo.get(id).map {
        case Some(p) =>
          Success(p)
        case None =>
          Failure(new RuntimeException("Product not found"))
      } pipeTo sender
    case UpdatePrice(info)  =>
      productRepo.update(info).map {
        case true => Success(ProductUpdated(info.id))
        case false => Failure(new RuntimeException("Product not found"))
      } pipeTo sender
  }
}
//#user-registry-actor
