package com.bot.models.json.bot.telegram

case class AnswerInlineQueryVenue(
                                   inline_query_id: String,
                                   results: List[InlineQueryResultVenue] = Nil,
                                   cache_time: Option[Int] = None,
                                   is_personal: Option[Boolean] = None,
                                   next_offset: Option[String] = None,
                                   switch_pm_text: Option[String] = None,
                                   switch_pm_parameter: Option[String] = None
                                 )

