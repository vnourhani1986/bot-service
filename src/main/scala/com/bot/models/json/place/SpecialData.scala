package com.bot.models.json.place

case class SpecialData(
                        title: Option[String] = None,
                        items: List[Item] = Nil
                      )
