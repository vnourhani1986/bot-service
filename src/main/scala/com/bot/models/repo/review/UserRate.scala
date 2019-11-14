package com.bot.models.repo.review

import java.time.LocalDateTime

case class UserRate(
                     id: Option[Long] = None,
                     userId: Long,
                     placeUUID: String,
                     questionIds: List[Long],
                     questionValues: List[Int],
                     average: Option[Double],
                     createdAt: LocalDateTime,
                     modifiedAt: Option[LocalDateTime] = None,
                     provider: Option[String] = Some("3net"),
                     disabled: Boolean = false,
                     deleted: Boolean = false
                   )
