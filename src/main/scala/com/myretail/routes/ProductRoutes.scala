package com.myretail.routes

import javax.ws.rs.Path

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{get, post, put}
import akka.http.scaladsl.server.directives.RouteDirectives.complete

import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.ask
import akka.util.Timeout
import com.myretail.JsonSupport
import com.myretail.repo.ProductInfo
import com.myretail.actor.ProductInfoActor._
import com.myretail.repo.ProductPrice
import io.swagger.annotations._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

case class ProductDetails(id: Int, name: String, current_price: ProductPrice)
//#product-routes-class
@Api(value = "/products", produces = "application/json")
@Path("/")
trait ProductRoutes extends JsonSupport {
  //#product-routes-class

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[ProductRoutes])

  // other dependencies that UserRoutes use
  def productInfoActor: ActorRef

  // Required by the `ask` (?) method below
  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration


  lazy val productRoutes: Route =
    insertProduct ~ updateProduct ~ getProduct

  @Path("/products")
  @ApiOperation(value = "Insert product details to NoSQL data store", notes = "Inserts the product details to cassandra",
    httpMethod = "POST", code = 201)
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "Product id and product price to be inserted", required = true,
      dataTypeClass = classOf[ProductInfo], paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "Product details inserted"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def insertProduct = {
    post {
      entity(as[ProductInfo]) { product =>
        val productInserted: Future[ProductInserted] =
          (productInfoActor ? InsertProduct(product)).mapTo[ProductInserted]
        onSuccess(productInserted) { performed =>
          log.info("Inserted product {}", product.id)
          complete((StatusCodes.Created))
        }
      }
    }
  }

  @Path("/products/{id}")
  @ApiOperation(value = "update product price in NoSQL data store", notes = "Updates the product price in cassandra",
    httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "Product id and product price to be updated", required = true,
      dataTypeClass = classOf[ProductInfo], paramType = "body"),
    new ApiImplicitParam(name = "id", required = true, dataType = "string", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Product details updated"),
    new ApiResponse(code = 500, message = "Internal server error"),
    new ApiResponse(code = 404, message = "Product not found")
  ))
  def updateProduct = {
    put {
      pathPrefix("products" / Segment) { pid =>
        entity(as[ProductInfo]) { product =>
          val productUpdated: Future[ProductUpdated] =
            (productInfoActor ? UpdatePrice(product.copy(id = pid.toInt))).mapTo[ProductUpdated]
          onComplete(productUpdated) {
            case Success(performed) =>
              log.info("Product Updated {}", product.id)
              complete((StatusCodes.OK))
            case Failure(ex) =>
              log.error(s"Error updating Product info ${product.id} {}", ex)
              complete((StatusCodes.NotFound))
          }
        }
      }
    }
  }

  @Path("/products/{id}")
  @ApiOperation(
    httpMethod = "GET", code = 200, value = "Get Product details", notes = "Combines the product details from datastore and redsky"
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", required = true, dataType = "string", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 404, message = "Product id not found"),
    new ApiResponse(code = 200, response = classOf[ProductDetails], message = "Returns the product details")))
  def getProduct: Route = {
    get {
      pathPrefix("products"/ Segment) { id => {
        //#retrieve-product-info
        val maybeProduct: Future[ProductInfo] =
          (productInfoActor ? GetProductPrice(id.toInt)).mapTo[ProductInfo]
        val productName: Future[String] =
          (productInfoActor ? GetProductName(id.toInt)).mapTo[String]
        val result: Future[ProductDetails] =
          for {
            info <- maybeProduct
            name <- productName
          } yield ProductDetails(info.id, name, info.current_price)
        onComplete(result) {
          case Success(productDetails) =>
            println("received product details in routes")
            complete(ToResponseMarshallable(productDetails))
          case Failure(ex) =>
            log.error(s"Error when retrieving product info $id {}", ex)
            complete((StatusCodes.NotFound, s"product $id not found"))
        }
      }
      }
      //#retrieve-user-info
    }
  }
}
