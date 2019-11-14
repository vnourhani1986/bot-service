package com.bot.models.json.bot.telegram

case class SendLocation(
                         chat_id: Int,
                         latitude: Float,
                         longitude: Float,
                         live_period: Option[Int] = None,
                         disable_notification: Option[Boolean] = None,
                         reply_to_message_id: Option[Int] = None,
                         reply_markup: Option[InlineKeyboardMarkup] = None
                       )

