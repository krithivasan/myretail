package com.myretail

import com.datastax.driver.core.{Cluster, Session}
import org.cassandraunit.CQLDataLoader
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet
import org.cassandraunit.utils.EmbeddedCassandraServerHelper

object EmbeddedCassandra {

  EmbeddedCassandraServerHelper.startEmbeddedCassandra(EmbeddedCassandraServerHelper.DEFAULT_CASSANDRA_YML_FILE, 30000)

  val KEYSPACE = "productinfo"

  lazy val embeddedCluster: Cluster =
    Cluster.builder().
      addContactPoint("127.0.0.1").
      withPort(9142).
      build()
  val t_session = embeddedCluster.connect()
  val dataLoader: CQLDataLoader = new CQLDataLoader(t_session)
  val session: Session = dataLoader.getSession()

  def loadDataSet(dataSet: String): Unit = {
    dataLoader.load(new ClassPathCQLDataSet(dataSet, KEYSPACE))
  }

  def close(): Unit = {
    embeddedCluster.close()
  }

}
