package com.bot.models.repo.user

import java.time.LocalDateTime

import spray.json.JsValue

case class UserActivity(
                         id: Option[Long] = None,
                         userId: Long,
                         chatId: Long,
                         entityId: Option[Long] = None,
                         action: String = "",
                         metaData: Option[JsValue] = None,
                         createdAt: LocalDateTime,
                         modifiedAt: Option[LocalDateTime] = None,
                         disabled: Boolean = false,
                         deleted: Boolean = false
                       )
