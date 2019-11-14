package com.bot.models.json.bot.telegram

case class SendMessage(
                        chat_id: Int,
                        text: String,
                        parse_mode: Option[String] = None,
                        disable_web_page_preview: Option[Boolean] = None,
                        disable_notification: Option[Boolean] = None,
                        reply_to_message_id: Option[Int] = None,
                        reply_markup: Option[InlineKeyboardMarkup] = None
                      )

case class SendMessage1(
                        chat_id: Int,
                        text: String,
                        parse_mode: Option[String] = None,
                        disable_web_page_preview: Option[Boolean] = None,
                        disable_notification: Option[Boolean] = None,
                        reply_to_message_id: Option[Int] = None,
                        reply_markup: Option[ReplyKeyboardMarkup] = None
                      )

case class SendMessage2(
                         chat_id: String,
                         text: String,
                         parse_mode: Option[String] = None,
                         disable_web_page_preview: Option[Boolean] = None,
                         disable_notification: Option[Boolean] = None,
                         reply_to_message_id: Option[Int] = None,
                         reply_markup: Option[ReplyKeyboardMarkup] = None
                       )