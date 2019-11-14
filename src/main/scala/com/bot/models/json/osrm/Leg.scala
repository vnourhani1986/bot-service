package com.bot.models.json.osrm

import spray.json.JsValue

case class Leg(
                steps: List[JsValue],
                distance: Float,
                duration: Float,
                summary: String,
                weight: Float
              )
