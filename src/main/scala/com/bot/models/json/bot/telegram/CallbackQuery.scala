package com.bot.models.json.bot.telegram

import spray.json.JsValue

case class CallbackQuery(
                          id: String,
                          from: User,
                          message: Option[JsValue], // todo: message object
                          inline_message_id: Option[String],
                          chat_instance: String,
                          data: Option[String],
                          game_short_name: Option[String]
                        )

