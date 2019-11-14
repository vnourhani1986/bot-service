package com.bot.models.repo.place

import java.time.LocalDateTime

case class Like(
                 id: Option[Long] = None,
                 userId: Long,
                 placeId: Long,
                 like: Boolean,
                 provider: Option[String] = Some("3net"),
                 createdAt: LocalDateTime,
                 modifiedAt: Option[LocalDateTime] = None,
                 deleted: Boolean = false
               )
