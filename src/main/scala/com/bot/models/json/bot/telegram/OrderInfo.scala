package com.bot.models.json.bot.telegram

case class OrderInfo(
                      name: Option[String],
                      phone_number: Option[String],
                      email: Option[String],
                      shipping_address: Option[ShippingAddress]
                    )

