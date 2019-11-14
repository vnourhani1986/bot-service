package com.bot.models.repo.zone

import java.time.LocalDateTime

case class Zone(
                 id: Option[Long] = None,
                 coordinate: Long,
                 lat: Double,
                 lng: Double,
                 placeIds: List[Long],
                 createdAt: LocalDateTime,
                 modifiedAt: Option[LocalDateTime] = None,
                 disabled: Boolean = false,
                 deleted: Boolean = false
               )