package com.bot.models.repo.filter

import java.time.LocalDateTime

import spray.json.JsValue

case class Filter(
                   id: Option[Long] = None,
                   title: String,
                   data: String,
                   metaData: Option[JsValue],
                   createdAt: LocalDateTime,
                   modifiedAt: Option[LocalDateTime] = None,
                   disabled: Boolean = false,
                   deleted: Boolean = false
                 )
