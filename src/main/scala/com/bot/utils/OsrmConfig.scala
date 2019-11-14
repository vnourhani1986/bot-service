package com.bot.utils

import akka.http.scaladsl.settings.ClientConnectionSettings
import com.bot.DI._

import scala.concurrent.duration._

object OsrmConfig {
  val host: String = config.getString("osrm.host")
  val port: Int = config.getInt("osrm.port")
  private val baseUrl: String = config.getString("osrm.api-base-url")
  private val getDrivingRoutesPath: String = config.getString("osrm.get-driving-routes-path")
  private val getDrivingTablePath: String = config.getString("osrm.get-driving-table-path")
  val getDrivingRoutesUrl: String = s"""$baseUrl$getDrivingRoutesPath"""
  val getDrivingTableUrl: String = s"""$baseUrl$getDrivingTablePath"""
  val clientConnectionSettings: ClientConnectionSettings = ClientConnectionSettings(system).withConnectingTimeout(180.second)
  val timeOffset: String = config.getString("osrm.time-offset")
}
