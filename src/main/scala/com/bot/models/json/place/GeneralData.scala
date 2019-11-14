package com.bot.models.json.place

case class GeneralData(
                        _links: Option[GLink] = None,
                        items: List[Item] = Nil
                      )

