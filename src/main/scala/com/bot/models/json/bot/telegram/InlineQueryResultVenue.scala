package com.bot.models.json.bot.telegram

import spray.json.JsValue

case class InlineQueryResultVenue(
                                   `type`: String,
                                   id: String,
                                   latitude: Float,
                                   longitude: Float,
                                   title: String,
                                   address: String,
                                   foursquare_id: Option[String] = None,
                                   foursquare_type: Option[String] = None,
                                   reply_markup: Option[InlineKeyboardMarkup] = None,
                                   input_message_content: Option[JsValue] = None,
                                   thumb_url: Option[String] = None,
                                   thumb_width: Option[Int] = None,
                                   thumb_height: Option[Int] = None
                                 )
