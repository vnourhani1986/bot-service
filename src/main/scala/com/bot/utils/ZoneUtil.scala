package com.bot.utils

import com.bot.DI._
import com.bot.models.repo.zone.Zone
import com.bot.repos.place.PlaceRepoImpl
import com.bot.repos.zone.ZoneRepoImpl

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object ZoneUtil {

  val (nwy, nwx) = (40d, 43d)
  val (swy, swx) = (24d, 43d)
  val (ney, nex) = (40d, 64d)
  val (sey, sex) = (24d, 64d)

  val ONE_K_EQ_LAT_DEG = 0.00904371732
  val ONE_K_EQ_LNG_DEG_UP = 0.01172662
  val ONE_K_EQ_LNG_DEG_DOWN = 0.00983324

  val NUMBER_OF_DIV_LNG = 12
  val NUMBER_OF_DIV_LAT = 11

  def zoneCalc(lat: Double, lng: Double): (Long, Double, Double) = {

    val (py, px) = (lat, lng)
    var x1 = nwx
    var x2 = nex
    var cxList = List.empty[Int]
    List.range(0, 12).map { _ =>
      val p = (x2 + x1) / 2d
      if (px > p) {
        x1 = p
        cxList = cxList ++ List(1)
      } else {
        x2 = p
        cxList = cxList ++ List(0)
      }
      p
    }

    var y1 = swy
    var y2 = nwy
    var cyList = List.empty[Int]
    List.range(0, 11).map { _ =>
      val p = (y2 + y1) / 2d
      if (py > p) {
        y1 = p
        cyList = cyList ++ List(1)
      } else {
        y2 = p
        cyList = cyList ++ List(0)
      }
      p
    }

    cyList = cyList ++ List(cyList.last)
    val list = cxList.zip(cyList)
    val coordinate = list.map {
      case (0, 1) => 1l
      case (1, 1) => 2l
      case (0, 0) => 3l
      case (1, 0) => 4l
    }.foldLeft(0l)((m, d) => m * 10l + d)

    (coordinate, (y2 + y1) / 2d, (x2 + x1) / 2d)

  }

  def updateZoneTable(): Unit = {

    List.range(0, 38).foreach { r =>
      val result1 = PlaceRepoImpl.get(r * 10000, 10000).map { ps =>
        ps.toList.foreach { p =>
          println(p.id)
          val (c, lat, lng) = zoneCalc(p.lat.getOrElse(0d), p.lng.getOrElse(0d))
          val result = for {
            opt <- ZoneRepoImpl.find(c)
            zoneId <- if (opt.isDefined) {
              ZoneRepoImpl.update(opt.get.copy(
                coordinate = c,
                placeIds = (opt.get.placeIds ++ List(p.id.get)).distinct,
                modifiedAt = DateTimeUtils.nowOpt
              ))
            } else {
              ZoneRepoImpl.save(Zone(
                coordinate = c,
                lat = lat,
                lng = lng,
                placeIds = List(p.id.get),
                createdAt = DateTimeUtils.now
              ))
            }
          } yield {
            zoneId
          }
          Await.result(result, Duration.Inf)
        }
      }
      Await.result(result1, Duration.Inf)
    }
  }

  def updateZoneTable(placeId: Long, lat: Double, lng: Double): Future[Option[Long]] = {

    val (c, zLat, zLng) = zoneCalc(lat, lng)

    for {
      opt <- ZoneRepoImpl.find(c)
      zoneId <- if (opt.isDefined) {
        ZoneRepoImpl.update(opt.get.copy(
          coordinate = c,
          placeIds = (opt.get.placeIds ++ List(placeId)).distinct,
          modifiedAt = DateTimeUtils.nowOpt
        ))
      } else {
        ZoneRepoImpl.save(Zone(
          coordinate = c,
          lat = zLat,
          lng = zLng,
          placeIds = List(placeId),
          createdAt = DateTimeUtils.now
        ))
      }
    } yield {
      zoneId
    }

  }

}
//
//object runner extends App {
//  println(ZoneUtil.zoneCalc( 35.68971252441406d, 51.38606643676758d))
//  ZoneUtil.updateZoneTable(329227, 35.68971252441406d, 51.38606643676758d).foreach(println)
//}