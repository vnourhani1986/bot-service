package com.bot.models.json.bot.telegram

case class KeyboardButton(
                           text: String,
                           request_contact: Option[Boolean] = None,
                           request_location: Option[Boolean] = None
                         )

