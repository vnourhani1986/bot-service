package com.bot.telegram

import akka.actor.{Actor, ActorSystem, Cancellable}
import akka.http.scaladsl.model.StatusCode
import akka.pattern.ask
import akka.util.Timeout
import com.bot.formats.Formats._
import com.bot.models.json.api.QueryResult
import com.bot.models.json.bot.telegram._
import com.bot.models.json.osrm.GetTableResult
import com.bot.models.repo.place.{Place, QueryPlace}
import com.bot.services.telegram.TelegramBotService.Messages.{GetNearByDistanceCalc, GetUpdates, _}
import com.bot.services.telegram.TelegramBotService.{callbackDispatcherActor, osrmActor, postgresActor}
import com.bot.utils.TelegramBotConfig
import com.typesafe.scalalogging.LazyLogging
import spray.json.JsValue

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

class FindPlaceActor(
                      implicit
                      system: ActorSystem,
                      ec: ExecutionContext,
                      timeout: Timeout
                    ) extends Actor with LazyLogging {

  val retryStep = 10
  val retryMax = 5
  var offset = 0
  var limit: Int = TelegramBotConfig.limit

  self ! Start()

  override def receive(): Receive = {

    case Start() =>
      logger.info(s"""start telegram bot place actor""")

    case GetNearByDistanceCalc(messageId, chatId, subGuilds, location, radio, page, viewLength, last, from) =>

      (for {
        ps <- (postgresActor ? GetNearPlaces(subGuilds, location.latitude, location.longitude, radio, viewLength * (page - 1), viewLength)).mapTo[(Seq[Place], Int)]
      } yield {
        ps
      }).onComplete {
        case scala.util.Success(l) =>
          val (places, totalLength) = l
          val totalPage = if (totalLength % viewLength == 0) {
            totalLength / viewLength
          } else {
            totalLength / viewLength + 1
          }
          val pg = if (last) totalPage else page
          val offset = (pg - 1) * viewLength
          if (pg <= totalPage && pg > 0) {
            callbackDispatcherActor ! SendPlaceListToCallbackActor(messageId, chatId, places.slice(offset, offset + viewLength), pg, totalPage, last, from)
          } else {
            callbackDispatcherActor ! SendPlaceListToCallbackActor(messageId, chatId, Nil, 1, 1, last = true, from)
          }
        case scala.util.Failure(error) =>
          logger.info(s"""get near place with error : ${error.getMessage}""")
      }

    case GetPlace(messageId, userId, placeId, from) =>

      (for {
        places <- (postgresActor ? GetPlaceToPostgresActor(placeId)).mapTo[Seq[Place]]
        placesWithLikes <- (postgresActor ? FindPlacesWithLikes(places)).mapTo[Seq[(Place, Int, Int)]]
      } yield {
        placesWithLikes
      }).onComplete {
        case scala.util.Success(l) =>
          callbackDispatcherActor ! SendCreateVenueToCallbackActor(messageId, userId, l, from)
        case scala.util.Failure(error) =>
          logger.info(s"""get place with error : ${error.getMessage}""")
      }

    case SendInlineQueryToFindPlaceActor(query) =>

      val ref = sender()
      (for {
        queryPlaces <- (postgresActor ? FindPlaceByQuery(query)).mapTo[Seq[QueryPlace]]
      } yield {
        queryPlaces
      }).onComplete {
        case scala.util.Success(qps) =>
          ref ! qps
        case scala.util.Failure(error) =>
          logger.info(s"""get inline query place with error : ${error.getMessage}""")
          ref ! Nil
      }

    case SendApiQueryToFindPlaceActor(query, lat, lng, page) =>

      val ref = sender()
      (for {
        queryPlaces <- (postgresActor ? FindPlaceByApiQuery(query, lat, lng, page)).mapTo[Seq[QueryPlace]]
      } yield {
        queryPlaces
      }).onComplete {
        case scala.util.Success(qps) =>
          ref ! qps.map { qp =>
            QueryResult(
              qp.placeId,
              qp.title,
              qp.address,
              qp.phone,
              qp.lat,
              qp.lng
            )
          }
        case scala.util.Failure(error) =>
          logger.info(s"""get inline query place with error : ${error.getMessage}""")
          ref ! Nil
      }


    case GetNearFromOSRM(messageId, chatId, subGuilds, location, radio, page, viewLength, last, from) =>

      val source = (location.longitude, location.latitude)
      (for {
        (placeIds, totalLength) <- (postgresActor ? GetPlaceIdsFromZone(location.latitude, location.longitude, radio, viewLength * (page - 1), viewLength)).mapTo[(Seq[Long], Int)]
        places <- (postgresActor ? GetBySubGuildAndIds(placeIds, subGuilds)).mapTo[Seq[Place]]
        destination = places.map(x => (x.lng.getOrElse(0d).toFloat, x.lat.getOrElse(0d).toFloat)).toList
        (status, body) <- (osrmActor ? GetDuration(source, destination)).mapTo[(StatusCode, JsValue)]
        _ = if (status != StatusCode.int2StatusCode(200)) new Exception
        getTableResult = body.convertTo[GetTableResult]
        sortedPlaces = if (getTableResult.code == "Ok") {
          getTableResult.durations.flatten.zip(places).sortBy(x => x._1).take(viewLength).map(_._2)
        } else {
          Nil
        }
      } yield {
        (sortedPlaces, totalLength)
      }).onComplete {
        case scala.util.Success(l) =>
          val (places, totalLength) = l
          val totalPage = if (totalLength % viewLength == 0) {
            totalLength / viewLength
          } else {
            totalLength / viewLength + 1
          }
          val offset = (page - 1) * viewLength
          if (page <= totalPage && page > 0) {
            callbackDispatcherActor ! SendPlaceListToCallbackActor(messageId, chatId, places.slice(offset, offset + viewLength), page, totalPage, last, from)
          } else {
            callbackDispatcherActor ! SendPlaceListToCallbackActor(messageId, chatId, Nil, 1, 1, last = true, from)
          }
        case scala.util.Failure(error) =>
          logger.info(s"""get near place from osrm with error : ${error.getMessage}""")
      }

    case GetPopPlaces(messageId, chatId, subGuilds, location, radio, page, viewLength, last, from) =>

      (for {
        ps <- (postgresActor ? GetPopPlaceBySubGuild(subGuilds, location.latitude, location.longitude, radio)).mapTo[(Seq[Place], Int)]
      } yield {
        ps
      }).onComplete {
        case scala.util.Success(l) =>
          val (places, totalLength) = l
          val totalPage = if (totalLength % viewLength == 0) {
            totalLength / viewLength
          } else {
            totalLength / viewLength + 1
          }
          val offset = (page - 1) * viewLength
          if (page <= totalPage && page > 0) {
            callbackDispatcherActor ! SendPlaceListToCallbackActor(messageId, chatId, places.slice(offset, offset + viewLength), page, totalPage, last, from)
          } else {
            callbackDispatcherActor ! SendPlaceListToCallbackActor(messageId, chatId, Nil, 1, 1, last = true, from)
          }
        case scala.util.Failure(error) =>
          logger.info(s"""get pop place with error : ${error.getMessage}""")
      }

    case _ =>
      logger.info(s"""welcome to telegram place actor""")

  }

  def getUpdates(time: FiniteDuration): Cancellable = {
    context.system.scheduler.scheduleOnce(time, self, GetUpdates())
  }

  def retry(time: FiniteDuration, message: SendMessage): Cancellable = {
    context.system.scheduler.scheduleOnce(time, self, message)
  }

  def retry(time: FiniteDuration, message: SendLocation): Cancellable = {
    context.system.scheduler.scheduleOnce(time, self, message)
  }

}

object FindPlaceActor {

  val LOG2: Double = Math.log(2)

  object Contents {

  }

}