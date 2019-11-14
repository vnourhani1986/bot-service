package com.bot.models.repo.guild

import java.time.LocalDateTime

case class Guild(
                  id: Option[Long] = None,
                  playerId: Option[Long] = None,
                  title: Option[String] = None,
                  public: Option[Int] = None,
                  icon: Option[String] = None,
                  version: Option[Double] = None,
                  template: Option[String] = None, // todo ???
                  equality: Option[Int] = None,
                  createdAt: LocalDateTime,
                  modifiedAt: Option[LocalDateTime] = None,
                  disabled: Boolean = true,
                  deleted: Boolean = false
                )
