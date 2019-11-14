package com.bot.models.repo.place

import java.time.LocalDateTime

case class Rate(
                 id: Option[Long] = None,
                 placeId: Option[Long],
                 average: Option[Double] = None,
                 count: Option[Int] = None,
                 provider: Option[String] = Some("3net"),
                 createdAt: LocalDateTime,
                 modifiedAt: Option[LocalDateTime] = None,
                 deleted: Boolean = false
               )