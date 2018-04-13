package com.example

//#user-routes-spec
//#test-top
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{MessageEntity, _}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.datastax.driver.core.Session
import com.example.QuickstartServer.{productRepo, system}
import com.example.routes.ProductRoutes
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import com.example.actor.ProductInfoActor
import com.example.repo.{ProductInfo, ProductPrice, ProductRepoImpl}

import scala.concurrent.duration._

//#set-up
class ProductRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
    with ProductRoutes {
  //#test-top
  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(5.seconds)

  EmbeddedCassandra.loadDataSet("db/T_PRODUCT_INFO_DDL_001.cql")
  val productRepo = new ProductRepoImpl {
    override val session: Session = EmbeddedCassandra.session
    override val keyspace: String = "productinfo"
  }
  override def afterAll(): Unit = {
    EmbeddedCassandra.close()
  }
  // Here we need to implement all the abstract members of UserRoutes.
  // We use the real UserRegistryActor to test it while we hit the Routes, 
  // but we could "mock" it by implementing it in-place or by using a TestProbe() 
  //override val productInfoActor: ActorRef =
   // system.actorOf(ProductInfoActor.props, "productInfo")
  override val productInfoActor: ActorRef = system.actorOf(ProductInfoActor.props(productRepo), "productInfoActor")


  lazy val routes = productRoutes

  //#set-up

  //#actual-test
  "ProductRoutes" should {
    "return product details if id is present (GET /products/13860428)" in {
      // note that there's no need for the host part in the uri:
      val id: String =  "13860428"
      val request = HttpRequest(uri = s"/products/${id}")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

      }
    }

    "return not found product details if id is not present (GET /products/1386042899)" in {
      // note that there's no need for the host part in the uri:
      val id: String =  "1386042899"
      val request = HttpRequest(uri = s"/products/${id}")

      request ~> routes ~> check {
        status should ===(StatusCodes.NotFound)

      }
    }

    "Insert product details (POST /products)" in {
      val productPrice = ProductInfo(13860428,ProductPrice(13.49,"USD"))
      val entity = Marshal(productPrice).to[MessageEntity].futureValue

      Post("/products").withEntity(entity) ~> routes ~> check {
        status should ===(StatusCodes.Created)
      }
    }

    "Update product details (PUT /products/13860428)" in {
      val productPrice = ProductInfo(13860428,ProductPrice(13.49,"USD"))
      val entity = Marshal(productPrice).to[MessageEntity].futureValue

      val id: String =  "13860428"

      Put(s"/products/${id}").withEntity(entity) ~> routes ~> check {
        status should ===(StatusCodes.OK)
      }
    }

    "Do not update product details if id ins not present (PUT /products/1386042899)" in {
      val productPrice = ProductInfo(1386042899,ProductPrice(13.49,"USD"))
      val entity = Marshal(productPrice).to[MessageEntity].futureValue

      val id: String =  "1386042899"

      Put(s"/products/${id}").withEntity(entity) ~> routes ~> check {
        status should ===(StatusCodes.NotFound)
      }
    }
    //#actual-test

    //#testing-post
   /* "be able to add users (POST /users)" in {
      val user = User("Kapi", 42, "jp")
      val userEntity = Marshal(user).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      // using the RequestBuilding DSL:
      val request = Post("/users").withEntity(userEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and we know what message we're expecting back:
        entityAs[String] should ===("""{"description":"User Kapi created."}""")
      }
    }
    //#testing-post

    "be able to remove users (DELETE /users)" in {
      // user the RequestBuilding DSL provided by ScalatestRouteSpec:
      val request = Delete(uri = "/users/Kapi")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should ===("""{"description":"User Kapi deleted."}""")
      }
    }
    //#actual-test
  }*/
  //#actual-test

  //#set-up
}
}
//#set-up
//#user-routes-spec
