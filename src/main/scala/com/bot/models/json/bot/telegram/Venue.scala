package com.bot.models.json.bot.telegram

case class Venue(
                  location: Location,
                  title: String,
                  address: String,
                  foursquare_id: Option[String] = None,
                  foursquare_type: Option[String] = None
                )

