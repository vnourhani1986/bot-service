package com.bot.models.repo.place

import java.time.LocalDateTime

case class UserPlace(
                      id: Option[Long] = None,
                      userId: Long,
                      placeId: Long,
                      provider: Option[String] = Some("3net-by-user"),
                      createdAt: LocalDateTime,
                      modifiedAt: Option[LocalDateTime] = None,
                      deleted: Boolean = false
                    )
