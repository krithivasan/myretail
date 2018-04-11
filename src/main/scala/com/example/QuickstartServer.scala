package com.example

//#quick-start-server
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.datastax.driver.core.Session
import com.example.actor.ProductInfoActor
import com.example.repo.ProductRepoImpl
import com.example.routes.ProductRoutes
import com.example.repo.CassandraConnection
import com.example.swagger.{ Site, SwaggerDocService }

//#main-class
object QuickstartServer extends App with ProductRoutes with Site {

  // set up ActorSystem and other dependencies here
  //#main-class
  //#server-bootstrapping
  implicit val system: ActorSystem = ActorSystem("mRetail-REST-API")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  //#server-bootstrapping

  val productRepo = new ProductRepoImpl {
    override lazy val session: Session = CassandraConnection.getSession

    override val keyspace: String = CassandraConnection.keyspace
  }

  val productInfoActor: ActorRef = system.actorOf(ProductInfoActor.props(productRepo), "productInfoActor")

  //#main-class
  // from the ProductRoutes trait
  lazy val routes: Route = productRoutes ~ SwaggerDocService.routes ~ site
  //#main-class

  //#http-server
  Http().bindAndHandle(routes, "0.0.0.0", 8080)

  println(s"Server online at http://localhost:8080/")

  Await.result(system.whenTerminated, Duration.Inf)
  //#http-server
  //#main-class
}
//#main-class
//#quick-start-server
