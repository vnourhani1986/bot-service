package com.bot.utils

import akka.http.scaladsl.settings.ClientConnectionSettings
import com.bot.DI._

import scala.concurrent.duration._

object TelegramBotConfig {
  val host: String = config.getString("telegram-bot.host")
  val name: String = config.getString("telegram-bot.name")
  private val baseUrl: String = config.getString("telegram-bot.api-base-url")
  private val getUpdatePath: String = config.getString("telegram-bot.get-update-path")
  private val sendMessagePath: String = config.getString("telegram-bot.send-message-path")
  private val sendLocationPath: String = config.getString("telegram-bot.send-location-path")
  private val sendVenuePath: String = config.getString("telegram-bot.send-venue-path")
  private val sendPhotoPath: String = config.getString("telegram-bot.send-photo-path")
  private val deleteMessagePath: String = config.getString("telegram-bot.delete-message-path")
  private val editMessageCaptionPath: String = config.getString("telegram-bot.edit-message-caption-path")
  private val sendAnswerCallbackQueryPath: String = config.getString("telegram-bot.send-answer-callback-query-path")
  private val sendAnswerInlineQueryPath: String = config.getString("telegram-bot.send-answer-inline-query-path")
  private val token: String = config.getString("telegram-bot.token")
  val limit: Int = config.getInt("telegram-bot.limit")
  val fetchPeriod: Int = config.getInt("telegram-bot.fetch-period")
  val getUpdatesUrl: String = s"""$baseUrl$token$getUpdatePath"""
  val sendMessageUrl: String = s"""$baseUrl$token$sendMessagePath"""
  val sendLocationUrl: String = s"""$baseUrl$token$sendLocationPath"""
  val sendVenueUrl: String = s"""$baseUrl$token$sendVenuePath"""
  val sendPhotoUrl: String = s"""$baseUrl$token$sendPhotoPath"""
  val deleteMessageUrl: String = s"""$baseUrl$token$deleteMessagePath"""
  val editMessageCaptionUrl: String = s"""$baseUrl$token$editMessageCaptionPath"""
  val sendAnswerCallbackQueryUrl: String = s"""$baseUrl$token$sendAnswerCallbackQueryPath"""
  val sendAnswerInlineQueryUrl: String = s"""$baseUrl$token$sendAnswerInlineQueryPath"""
  // photos
  val startCommandPhoto: String = config.getString("telegram-bot.photos.start-command")
  val setGuildPhoto: String = config.getString("telegram-bot.photos.set-guild")
  val setSubGuildPhoto: String = config.getString("telegram-bot.photos.set-sub-guild")
  val setFilterPhoto: String = config.getString("telegram-bot.photos.set-filter")
  val inlineSearchPhoto: String = config.getString("telegram-bot.photos.inline-search")
  val setDistancePhoto: String = config.getString("telegram-bot.photos.set-distance")
  val setLocationPhoto: String = config.getString("telegram-bot.photos.set-location")
  val placeListPhoto: String = config.getString("telegram-bot.photos.place-list")
  val setPointPhoto: String = config.getString("telegram-bot.photos.set-point")
  val showReviewPhoto: String = config.getString("telegram-bot.photos.show-review")
  val setReviewPhoto: String = config.getString("telegram-bot.photos.set-review")
  val addPlacePhoto: String = config.getString("telegram-bot.photos.add-place")
  val editPlacePhoto: String = config.getString("telegram-bot.photos.edit-place")
  val selectFieldEditPlacePhoto: String = config.getString("telegram-bot.photos.select-field-edit-place")
  val successPhoto: String = config.getString("telegram-bot.photos.success")
  val failPhoto: String = config.getString("telegram-bot.photos.fail")
  //
  val clientConnectionSettings: ClientConnectionSettings = ClientConnectionSettings(system).withConnectingTimeout(180.second)
  val timeOffset: String = config.getString("telegram-bot.time-offset")
}
