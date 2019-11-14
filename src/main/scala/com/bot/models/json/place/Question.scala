package com.bot.models.json.place

case class Question(
                     overall: Option[Int] = None,
                     id: Option[Long] = None,
                     text: Option[String] = None,
                     title: Option[String] = None
                   )
