package com.bot.models.json.place


case class Item(
                 uuid: Option[String] = None,
                 title: Option[String] = None,
                 address: Option[String] = None,
                 phone: Option[String] = None,
                 lat: Option[Double] = None,
                 lng: Option[Double] = None,
                 rates: Option[Rate] = None,
                 guilds: List[Guild] = Nil,
                 logo: Option[Logo] = None,
                 commentsCount: Option[Int] = None,
                 has_active_contract: Option[Boolean] = None,
                 hygiene_status: Option[String] = None,
                 _links: Option[Link] = None,
                 checkinCount: Option[Int]
               )
