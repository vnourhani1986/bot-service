package com.bot.models.json.callback

case class Param(
                  gId: Option[Long] = None,
                  sgId: Option[Long] = None,
                  fId: Option[Long] = None,
                  pId: Option[Long] = None,
                  dst: Option[Double] = None,
                  pg: Option[Int] = None,
                  qId: Option[Long] = None,
                  rId: Option[Long] = None,
                  mId: Option[Long] = None,
                  lat: Option[Float] = None,
                  lng: Option[Float] = None,
                  pIds: Option[List[Long]] = None,
                  rate: Option[Int] = None
                )