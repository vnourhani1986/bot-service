package com.bot.models.json.bot.telegram

case class User(
                 id: Int,
                 is_bot: Boolean,
                 first_name: Option[String],
                 last_name: Option[String],
                 username: Option[String],
                 language_code: Option[String]
               )

