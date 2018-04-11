package com.example.util

import com.typesafe.config.ConfigFactory

object EnvironmentService {
  val conf = ConfigFactory.load("Environment.conf")

  /**
   * Provides the value read from conf file(Environment.conf)
   *
   * @param key
   */

  def getValue(key: String): String = conf.getString(key)

  def getBooleanValue(key: String): Boolean = conf.getBoolean(key)
}
