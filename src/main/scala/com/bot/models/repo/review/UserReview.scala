package com.bot.models.repo.review

import java.sql.Timestamp
import java.time.LocalDateTime

import com.bot.utils.DateTimeUtils
import spray.json.{JsNull, JsValue}

case class UserReview(
                       id: Option[Long] = None,
                       reviewId: Long,
                       userId: Long,
                       placeUUID: String,
                       content: Option[String],
                       images: JsValue = JsNull,
                       repliesCount: Option[Int] = None,
                       repliesItems: Option[JsValue] = None,
                       likesCount: Option[Int] = None,
                       likesItems: Option[JsValue] = None,
                       isLikeByUser: Option[Boolean] = None,
                       reviewCreatedAt: Option[Timestamp] = None,
                       links: Option[JsValue] = None,
                       createdAt: LocalDateTime = DateTimeUtils.now,
                       modifiedAt: Option[LocalDateTime] = DateTimeUtils.nowOpt,
                       provider: Option[String] = Some("3net"),
                       disabled: Boolean = false,
                       deleted: Boolean = false
                 )
