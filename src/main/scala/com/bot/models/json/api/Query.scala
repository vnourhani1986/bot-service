package com.bot.models.json.api

case class Query(
                  query: String,
                  lat: Option[Double] = None,
                  lng: Option[Double] = None,
                  page: Int = 0
                )

