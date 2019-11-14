package com.bot.models.repo.place

import java.time.LocalDateTime

case class Question(
                     id: Option[Long] = None,
                     rateId: Option[Long],
                     overall: Option[Int] = None,
                     text: Option[String] = None,
                     title: Option[String] = None,
                     provider: Option[String] = Some("3net"),
                     createdAt: LocalDateTime,
                     modifiedAt: Option[LocalDateTime] = None,
                     deleted: Boolean = false
                   )
