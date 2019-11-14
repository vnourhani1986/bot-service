package com.bot.models.repo.review

import java.time.LocalDateTime

case class QuestionSubGuild(
                             id: Option[Long] = None,
                             subGuildId: Long,
                             questionId: Long,
                             createdAt: LocalDateTime,
                             modifiedAt: Option[LocalDateTime] = None,
                             deleted: Boolean = false
                           )
