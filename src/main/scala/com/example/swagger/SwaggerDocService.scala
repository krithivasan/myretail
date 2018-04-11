package com.example.swagger

import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import io.swagger.models.ExternalDocs
import com.example.routes.ProductRoutes

object SwaggerDocService extends SwaggerHttpService {
  override def apiDocsPath: String = "mRetail/api-docs"
  override val apiClasses = Set(classOf[ProductRoutes])
  override val host = scala.util.Properties.envOrElse("DRP_EV_APP_DNS_URL", "localhost:8080")
  override val info = Info(title = "mRetail REST API", description = "This is a RestFul service to retrieve product information.", version = "1.0")
  override val externalDocs = Some(new ExternalDocs("Core Docs", "https://confluence.metrosystems.net"))
  override val unwantedDefinitions = Seq("Function1", "Function1RequestContextFutureRouteResult")
}
