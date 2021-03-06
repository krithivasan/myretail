package com.myretail.swagger

import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import io.swagger.models.ExternalDocs
import com.myretail.routes.ProductRoutes

object SwaggerDocService extends SwaggerHttpService {
  override def apiDocsPath: String = "myRetail/api-docs"
  override val apiClasses = Set(classOf[ProductRoutes])
  override val host = scala.util.Properties.envOrElse("DRP_EV_APP_DNS_URL", "localhost:8080")
  override val info = Info(title = "myRetail REST API", description = "This is a RestFul service to retrieve product information.", version = "1.0")
  override val externalDocs = Some(new ExternalDocs("Core Docs", "https://github.com/krithivasan/myretail"))
  override val unwantedDefinitions = Seq("Function1", "Function1RequestContextFutureRouteResult")
}
