package com.bot.models.json.bot.telegram

case class InputTextMessageContent(
                                    message_text: String,
                                    parse_mode: Option[String] = None,
                                    disable_web_page_preview: Option[Float] = None
                                  )
