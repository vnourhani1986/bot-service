package com.bot.models.repo.place

import java.time.LocalDateTime

case class Place(
                  id: Option[Long] = None,
                  cityId: Long,
                  childGuildIds: List[Long] = Nil,
                  uuid: Option[String] = None,
                  title: Option[String] = None,
                  address: Option[String] = None,
                  phone: Option[String] = None,
                  lat: Option[Double] = None,
                  lng: Option[Double] = None,
                  logo: Option[Logo] = None,
                  commentsCount: Option[Int] = None,
                  hasActiveContract: Option[Boolean] = None,
                  hygieneStatus: Option[String] = None,
                  links: Option[Link] = None,
                  provider: Option[String] = Some("3net"),
                  checkInCount: Option[Int] = None,
                  createdAt: LocalDateTime,
                  modifiedAt: Option[LocalDateTime] = None,
                  deleted: Boolean = false
                )