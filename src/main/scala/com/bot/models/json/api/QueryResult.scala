package com.bot.models.json.api

case class QueryResult(
                        placeId: Long,
                        title: Option[String] = None,
                        address: Option[String] = None,
                        phone: Option[String] = None,
                        lat: Option[Double] = None,
                        lng: Option[Double] = None
                      )

