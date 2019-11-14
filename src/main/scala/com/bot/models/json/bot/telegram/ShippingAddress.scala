package com.bot.models.json.bot.telegram

case class ShippingAddress(
                            country_code: String,
                            state: String,
                            city: String,
                            street_line1: String,
                            street_line2: String,
                            post_code: String
                          )

