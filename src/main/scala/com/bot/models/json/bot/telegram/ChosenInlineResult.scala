package com.bot.models.json.bot.telegram

case class ChosenInlineResult(
                               result_id: String,
                               from: User,
                               location: Option[Location],
                               inline_message_id: Option[String],
                               query: String
                             )

