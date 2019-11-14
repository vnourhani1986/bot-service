package com.bot.utils

import akka.http.scaladsl.settings.ClientConnectionSettings
import com.bot.DI._

import scala.concurrent.duration._

object CrawlerConfig {
  val host: String = config.getString("dunro.host")
  private val baseUrl: String = config.getString("dunro.api-base-url")
  val searchUrl: String = s"""$baseUrl${config.getString("dunro.search-url")}"""
  val guildUrl: String = s"""$baseUrl${config.getString("dunro.guild-url")}"""
  val clientConnectionSettings: ClientConnectionSettings = ClientConnectionSettings(system).withConnectingTimeout(180.second)
  val timeOffset: String = config.getString("dunro.time-offset")
}
