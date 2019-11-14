package com.bot.models.json.osrm

import spray.json.JsValue

case class GetTableResult(
                            code: String,
                            durations: List[List[Float]],
                            destinations: JsValue,
                            sources: JsValue
                          )

