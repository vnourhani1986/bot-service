package com.bot.models.repo.place

import java.time.LocalDateTime

case class QueryPlace(
                       id: Option[Long] = None,
                       placeId: Long,
                       placeUUID: Option[String] = None,
                       subGuilds: Option[String],
                       title: Option[String] = None,
                       address: Option[String] = None,
                       phone: Option[String] = None,
                       lat: Option[Double] = None,
                       lng: Option[Double] = None,
                       logo: Option[Logo] = None,
                       provider: Option[String] = Some("3net"),
                       createdAt: LocalDateTime,
                       modifiedAt: Option[LocalDateTime] = None,
                       deleted: Boolean = false
                     )