package com.bot.models.repo.review

import java.time.LocalDateTime

import com.bot.utils.DateTimeUtils

case class UserQuestion(
                         id: Option[Long] = None,
                         questionId: Long,
                         title: String,
                         createdAt: LocalDateTime,
                         modifiedAt: Option[LocalDateTime] = DateTimeUtils.nowOpt,
                         provider: Option[String] = Some("3net"),
                         disabled: Boolean = false,
                         deleted: Boolean = false
                       )
