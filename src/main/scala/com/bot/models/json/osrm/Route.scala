package com.bot.models.json.osrm

case class Route(
                   geometry: Option[String],
                   legs: List[Leg],
                   distance: Option[Float],
                   duration: Option[Float],
                   weight_name: Option[String],
                   weight: Option[Float]
                 )
