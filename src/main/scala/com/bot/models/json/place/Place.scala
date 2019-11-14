package com.bot.models.json.place

import spray.json.JsValue

case class Place(
                  generalData: Option[GeneralData] = None,
                  specialData: Option[SpecialData] = None,
                  filters: Option[JsValue] = None
                )

