package com.bot.models.json.bot.telegram

import spray.json.JsValue

case class PreCheckoutQuery(
                             id: String,
                             from: User,
                             currency: String,
                             total_amount: Int,
                             invoice_payload: String,
                             shipping_option_id: Option[String],
                             order_info: Option[JsValue]
                           )

