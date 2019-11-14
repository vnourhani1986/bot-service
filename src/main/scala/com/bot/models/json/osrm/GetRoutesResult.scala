package com.bot.models.json.osrm

import spray.json.JsValue

case class GetRoutesResult(
                            code: String,
                            routes: List[Route],
                            waypoints: JsValue
                          )

