package com.bot.models.repo.user

import java.time.LocalDateTime

case class UserTracking(
                         id: Option[Long] = None,
                         userId: Long,
                         lat: Double,
                         lng: Double,
                         createdAt: LocalDateTime,
                         modifiedAt: Option[LocalDateTime] = None,
                         disabled: Boolean = false,
                         deleted: Boolean = false
                       )
