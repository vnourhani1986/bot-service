package com.bot.utils

import com.bot.DI._
import com.bot.models.repo.place.{Place, QueryPlace}
import com.bot.repos.guild._3net.SubGuildRepoImpl
import com.bot.repos.place.{PlaceRepoImpl, QueryPlaceRepoImpl}
import com.typesafe.scalalogging.{LazyLogging, Logger}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object QueryPlaceUtil extends LazyLogging {

  def updateQueryPlaceTable(): Unit = {

    List.range(0, 38).foreach { r =>
      val result1 = PlaceRepoImpl.get(r * 10000, 10000).map { places =>
        places.foreach { place =>
          println(place.title, place.childGuildIds)
          val result2 = for {
            queryPlace <- QueryPlaceRepoImpl.findByPlaceId(place.id.get)
            _ <- if (queryPlace.isDefined) Future.failed(new Exception) else Future.successful()
            subGuilds <- SubGuildRepoImpl.find(place.childGuildIds)
            sGuilds = subGuilds.foldRight("")((s, g) => s"""${s.title.getOrElse("")} $g""")
            query = QueryPlace(
              placeId = place.id.get,
              placeUUID = place.uuid,
              subGuilds = Some(sGuilds),
              title = place.title,
              address = place.address,
              phone = place.phone,
              lat = place.lat,
              lng = place.lng,
              logo = place.logo,
              createdAt = DateTimeUtils.now
            )
            _ <- QueryPlaceRepoImpl.save(query)
          } yield {
            println(place.id)
          }
          val result = result2.recover {
            case _: Throwable =>
              println(place.id)
          }
          Await.result(result, Duration.Inf)
        }
      }
      Await.result(result1, Duration.Inf)
    }
  }

  def updateQueryPlaceTable(place: Place): Future[Option[Long]] = {

    val result = for {
      subGuilds <- SubGuildRepoImpl.find(place.childGuildIds)
      sGuilds = subGuilds.foldRight("")((s, g) => s"""${s.title.getOrElse("")} $g""")
      query = QueryPlace(
        placeId = place.id.get,
        placeUUID = place.uuid,
        subGuilds = Some(sGuilds),
        title = place.title,
        address = place.address,
        phone = place.phone,
        lat = place.lat,
        lng = place.lng,
        logo = place.logo,
        createdAt = DateTimeUtils.now
      )
      queryPlace <- QueryPlaceRepoImpl.findByPlaceId(place.id.get)
      id <- if (queryPlace.isDefined) {
        QueryPlaceRepoImpl.update(query.copy(id = queryPlace.flatMap(_.id), deleted = queryPlace.get.deleted))
      } else {
        QueryPlaceRepoImpl.save(query.copy(deleted = true))
      }
    } yield {
      id
    }

    result.recover {
      case error: Throwable =>
        logger.info(s"""update query place table with error: ${error.getMessage}""")
        None
    }

  }

}

//
//object runner1 extends App {
//
//  for{
//    place <- PlaceRepoImpl.findById(329227)
//    placeQueryId <- QueryPlaceUtil.updateQueryPlaceTable(place.get)
//  } yield {
//    println(place.get.id, placeQueryId)
//  }
//
//}
