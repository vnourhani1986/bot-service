package com.bot.models.json.bot.telegram

case class InlineQueryResultArticle(
                                     `type`: String,
                                     id: String,
                                     title: String,
                                     input_message_content: InputTextMessageContent,
                                     reply_markup: Option[InlineKeyboardMarkup] = None,
                                     url: Option[String] = None,
                                     hide_url: Option[String] = None,
                                     description: Option[String] = None,
                                     thumb_url: Option[String] = None,
                                     thumb_width: Option[Int] = None,
                                     thumb_height: Option[Int] = None
                                   )
