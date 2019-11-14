package com.bot.models.json.bot.telegram

case class EditMessageCaption(
                               chat_id: Int,
                               message_id: Option[Int] = None,
                               inline_message_id: Option[String] = None,
                               caption: Option[String] = None,
                               parse_mode: Option[String] = None,
                               reply_markup: Option[InlineKeyboardMarkup] = None
                             )

