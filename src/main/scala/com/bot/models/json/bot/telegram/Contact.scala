package com.bot.models.json.bot.telegram

case class Contact(
                    phone_number: String,
                    first_name: String,
                    last_name: Option[String],
                    user_id: Int,
                    vcard: Option[String]
                  )

