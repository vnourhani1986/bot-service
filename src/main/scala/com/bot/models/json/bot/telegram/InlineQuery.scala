package com.bot.models.json.bot.telegram

case class InlineQuery(
                        id: String,
                        from: User,
                        location: Option[Location],
                        query: String,
                        offset: String
                      )

