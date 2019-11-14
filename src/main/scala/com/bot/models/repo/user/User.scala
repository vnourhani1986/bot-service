package com.bot.models.repo.user

import java.time.{LocalDate, LocalDateTime}

import spray.json.JsValue

case class User(
                 id: Option[Long] = None,
                 userName: Option[String] = None,
                 createdAt: LocalDateTime,
                 modifiedAt: Option[LocalDateTime] = None,
                 firstName: Option[String] = None,
                 lastName: Option[String] = None,
                 isBot: Boolean = false,
                 email: Option[String] = None,
                 mobileNo: Option[String] = None,
                 langCode: Option[String] = None,
                 birthDate: Option[LocalDate] = None,
                 gender: Option[String] = None,
                 provider: Option[String] = Some("3net"),
                 score: Int = 0,
                 avatar: Option[JsValue] = None,
                 totalReviews: Int = 0,
                 totalViews: Int = 0,
                 totalLikes: Int = 0,
                 disabled: Boolean = false,
                 deleted: Boolean = false
               )
