package com.example.repo

import com.datastax.driver.core.{Row, Session}
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.example.routes.ProductDetails

import scala.concurrent.Future

case class ProductInfo(id: Int, price: ProductPrice)
case class ProductPrice(value: Double, currency_code: String)

trait ProductRepo {
  def insert(info: ProductInfo): Future[Boolean]
  def get(id: Int): Future[Option[ProductInfo]]
  def update(info: ProductInfo): Future[Boolean]
}

trait ProductRepoImpl extends ProductRepo {
  import GuavaFutures._
  import scala.concurrent.ExecutionContext.Implicits.global

  def keyspace: String

  def session: Session

  val T_PRODUCT_INFO = "product_info"

  def insert(info: ProductInfo): Future[Boolean] = {

    val statement = QueryBuilder.insertInto(keyspace, T_PRODUCT_INFO)
      .value("id", info.id)
      .value("value", info.price.value)
      .value("currency_code", info.price.currency_code)
    session.executeAsync(statement).asScala.map(_.wasApplied())
  }

  def get(id: Int): Future[Option[ProductInfo]] = {
    val statement = QueryBuilder.select().from(keyspace, T_PRODUCT_INFO).where(QueryBuilder.eq("id", id))
    val results = session.executeAsync(statement).asScala

    results.map { r =>
      r.isExhausted match {
        case true => None
        case false =>
          val row = r.one()
          Option(ProductInfo(row.getInt("id"), ProductPrice(row.getDouble("value"), row.getString("currency_code"))))
      }
    }
  }

  def update(info: ProductInfo ): Future[Boolean] = {
    get(info.id).flatMap {
      case Some(_) =>
        val statement = QueryBuilder.update(keyspace, T_PRODUCT_INFO)
        statement.`with`(QueryBuilder.set("value",info.price.value))
        statement.`with`(QueryBuilder.set("currency_code",info.price.currency_code))
          .where(QueryBuilder.eq("id",info.id))
        session.executeAsync(statement).asScala().map(_.wasApplied())
      case None => Future(false)
    }
  }
}
