package com.bot.models.repo.city

import java.time.LocalDateTime

case class City(
                 id: Option[Long] = None,
                 faName: String,
                 enName: Option[String] = None,
                 createdAt: LocalDateTime,
                 modifiedAt: Option[LocalDateTime] = None,
                 deleted: Boolean = false
               )
