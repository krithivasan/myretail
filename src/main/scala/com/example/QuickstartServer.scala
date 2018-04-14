package com.example

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.routing.FromConfig
import akka.stream.ActorMaterializer
import com.datastax.driver.core.Session
import com.example.actor.ProductInfoActor
import com.example.repo.ProductRepoImpl
import com.example.routes.ProductRoutes
import com.example.repo.CassandraConnection
import com.example.swagger.{Site, SwaggerDocService}

//#main-class
object QuickstartServer extends App with ProductRoutes with Site {


  implicit val system: ActorSystem = ActorSystem("myRetail-REST-API")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val productRepo = new ProductRepoImpl {
    override lazy val session: Session = CassandraConnection.getSession

    override val keyspace: String = CassandraConnection.keyspace
  }

  val productInfoActor: ActorRef = system.actorOf(FromConfig.props(ProductInfoActor.props(productRepo)),"productInfoActor")


  lazy val routes: Route = productRoutes ~ SwaggerDocService.routes ~ site

  Http().bindAndHandle(routes, "0.0.0.0", 8080)

  println(s"Server online at http://localhost:8080/")

  Await.result(system.whenTerminated, Duration.Inf)

}

