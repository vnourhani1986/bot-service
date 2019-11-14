package com.bot.models.json.bot.telegram

case class MessageEntity(
                          `type`: String,
                          offset: Int,
                          length: Int,
                          url: Option[String],
                          user: Option[User]
                        )

