package com.bot.repos

import java.sql.{Date, Timestamp}
import java.time.{LocalDate, LocalDateTime}

import com.bot.api.Messages.BusinessName
import com.bot.formats.Formats._
import com.bot.models.json.bot.telegram.Chat
import com.bot.models.repo.place.{Link, Logo}
import com.bot.utils.PostgresProfiler.api._
import spray.json._

trait SlickSupport {

  implicit val timestampLocalDateTimeMapper: BaseColumnType[LocalDateTime] = MappedColumnType.base[LocalDateTime, Timestamp](
    { localDateTime => Timestamp.valueOf(localDateTime) },
    { timeStamp => timeStamp.toLocalDateTime }
  )

  implicit val dateMapper: BaseColumnType[Date] = MappedColumnType.base[Date, String](_.toString, Date.valueOf)
  implicit val localDateMapper: BaseColumnType[LocalDate] = MappedColumnType.base[LocalDate, String](_.toString, LocalDate.parse(_, shortDateFormatter))

  implicit val businessNameMapper: BaseColumnType[BusinessName] = MappedColumnType.base[BusinessName, JsValue](
    c => c.toJson,
    j => j.convertTo[BusinessName]
  )

  implicit val logoMapper: BaseColumnType[Logo] = MappedColumnType.base[Logo, JsValue](
    logo => logo.toJson,
    json => json.convertTo[Logo]
  )

  implicit val linkMapper: BaseColumnType[Link] = MappedColumnType.base[Link, JsValue](
    link => link.toJson,
    json => json.convertTo[Link]
  )

  implicit val chatMapper: BaseColumnType[Chat] = MappedColumnType.base[Chat, JsValue](
    chat => chat.toJson,
    json => json.convertTo[Chat]
  )

}
