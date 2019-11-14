package com.bot.models.json.bot.telegram

import spray.json.JsValue

case class InlineKeyboardButton(
                                 text: String,
                                 url: Option[String] = None,
                                 login_url: Option[JsValue] = None, // todo: login_url object
                                 callback_data: Option[String] = None,
                                 switch_inline_query: Option[String] = None,
                                 switch_inline_query_current_chat: Option[String] = None,
                                 callback_game: Option[JsValue] = None, // todo: callback_game object
                                 pay: Option[Boolean] = None
                               )
