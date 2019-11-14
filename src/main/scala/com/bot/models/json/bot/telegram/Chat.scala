package com.bot.models.json.bot.telegram

import spray.json.JsValue

case class Chat(
                 id: Int,
                 `type`: String,
                 title: Option[String],
                 username: Option[String],
                 first_name: Option[String],
                 last_name: Option[String],
                 photo: Option[JsValue], // todo: create ChatPhoto object
                 description: Option[String],
                 invite_link: Option[String],
                 pinned_message: Option[JsValue],
                 permissions: Option[JsValue], // todo: create ChatPermissions object
                 sticker_set_name: Option[String],
                 can_set_sticker_set: Option[String]
               )

