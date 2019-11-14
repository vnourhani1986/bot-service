package com.bot.models.json.bot.telegram

case class SendPhoto(
                      chat_id: Int,
                      photo: String,
                      caption: Option[String] = None,
                      parse_mode: Option[String] = None,
                      disable_notification: Option[Boolean] = None,
                      reply_to_message_id: Option[Int] = None,
                      reply_markup: Option[InlineKeyboardMarkup] = None
                    )

case class SendPhoto1(
                      chat_id: Int,
                      photo: String,
                      caption: Option[String] = None,
                      parse_mode: Option[String] = None,
                      disable_notification: Option[Boolean] = None,
                      reply_to_message_id: Option[Int] = None,
                      reply_markup: Option[ReplyKeyboardMarkup] = None
                    )
