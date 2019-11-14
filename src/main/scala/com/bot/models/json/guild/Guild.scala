package com.bot.models.json.guild

case class Guild(
                  status: Boolean,
                  message: Option[String],
                  data: List[Data],
                  redirect: Option[String]
                )
