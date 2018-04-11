package com.example.repo

import java.net.URI

import com.datastax.driver.core._
import com.example.util.EnvironmentService

object CassandraConnection {
  private val connectionString = EnvironmentService.getValue("cassandra.config.url")
  private val uri = new URI(connectionString)

  private val additionalHosts = Option(uri.getQuery) match {
    case Some(query) => query.split("&").map(_.split("="))
      .filter(param => param(0) == "host").map(param => param(1)).toSeq
    case None => Seq.empty
  }

  val hosts = Seq(uri.getHost) ++ additionalHosts
  val port = uri.getPort
  val keyspace = uri.getPath.substring(1)

  lazy val clusterBuilder: Cluster.Builder = Cluster.builder()
    .addContactPoints(hosts.toArray: _*)
    .withPort(port)
    .withQueryOptions(new QueryOptions()
      .setConsistencyLevel(ConsistencyLevel.LOCAL_ONE)
      .setSerialConsistencyLevel(ConsistencyLevel.LOCAL_SERIAL))
    .withPoolingOptions(new PoolingOptions()
      .setConnectionsPerHost(
        HostDistance.LOCAL,
      EnvironmentService.getValue("cassandra.config.pool.min-connection").toInt,
      EnvironmentService.getValue("cassandra.config.pool.max-connection").toInt
      )
      .setConnectionsPerHost(
        HostDistance.REMOTE,
      EnvironmentService.getValue("cassandra.config.pool.min-connection").toInt,
      EnvironmentService.getValue("cassandra.config.pool.max-connection").toInt
      ))

  lazy val cluster: Cluster = updateCredentials(clusterBuilder).build()

  def updateCredentials(builder: Cluster.Builder): Cluster.Builder = {
    Option(uri.getUserInfo).map(_.isEmpty) match {
      case Some(false) =>
        val u = uri.getUserInfo.split(":")
        builder.withCredentials(u(0), u(1))
      case _ => builder
    }
  }

  private lazy val session: Session = createSession

  def createSession: Session = {
    createSession(keyspace)
  }

  def createSession(keyspace: String): Session = {
    cluster.connect(keyspace)
  }

  def closeSession(session: Session): Unit = session.close()

  def getSession: Session = {
    session
  }

  def closeAll(): Unit = {
    session.close()
    cluster.close()
  }

}
