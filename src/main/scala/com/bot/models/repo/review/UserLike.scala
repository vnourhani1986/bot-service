package com.bot.models.repo.review

import java.time.LocalDateTime

case class UserLike(
                     id: Option[Long] = None,
                     userId: Long,
                     reviewId: Long,
                     like: Boolean,
                     provider: Option[String] = Some("3net"),
                     createdAt: LocalDateTime,
                     modifiedAt: Option[LocalDateTime] = None,
                     deleted: Boolean = false
                   )
