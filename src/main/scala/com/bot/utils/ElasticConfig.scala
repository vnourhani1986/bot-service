package com.bot.utils

import akka.http.scaladsl.settings.ClientConnectionSettings
import com.bot.DI._

import scala.concurrent.duration._

object ElasticConfig {
  val host: String = config.getString("elastic-search.host")
  val port: Int = config.getInt("elastic-search.port")
  private val baseUrl: String = config.getString("elastic-search.api-base-url")
  val postIndex: String = s"""$baseUrl"""
  val clientConnectionSettings: ClientConnectionSettings = ClientConnectionSettings(system).withConnectingTimeout(180.second)
  val timeOffset: String = config.getString("elastic-search.time-offset")
}
