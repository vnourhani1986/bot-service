package com.bot.models.repo.history

import java.time.LocalDateTime

case class CrawlerHistory(
                           id: Option[Long] = None,
                           cityId: Option[Long] = None,
                           placeId: Option[Long] = None,
                           childGuildId: Option[Long] = None,
                           parentGuildId: Option[Long] = None,
                           cityName: Option[String] = None,
                           placeName: Option[String] = None,
                           childGuildName: Option[String] = None,
                           parentGuildName: Option[String] = None,
                           createdAt: LocalDateTime,
                           modifiedAt: Option[LocalDateTime] = None,
                           deleted: Boolean = false
                         )
