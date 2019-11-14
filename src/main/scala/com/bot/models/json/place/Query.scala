package com.bot.models.json.place

case class Query(
                  page: Int,
                  q: Option[String] = None,
                  area: Option[String] = None,
                  current_location: Option[String] = None
                )
