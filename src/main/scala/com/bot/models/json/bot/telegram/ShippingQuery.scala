package com.bot.models.json.bot.telegram

import spray.json.JsValue

case class ShippingQuery(
                          id: String,
                          from: User,
                          invoice_payload: String,
                          shipping_address: JsValue
                        )

