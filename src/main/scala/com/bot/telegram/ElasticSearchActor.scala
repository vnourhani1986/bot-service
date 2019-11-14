package com.bot.telegram

import java.time.LocalDateTime

import akka.actor.{Actor, ActorSystem, Cancellable}
import akka.http.scaladsl.model.StatusCodes
import akka.util.Timeout
import com.bot.formats.Formats._
import com.bot.services.elastic.ElasticSearchApi
import com.bot.services.telegram.TelegramBotService.Messages._
import com.bot.utils.{DateTimeUtils, ElasticConfig}
import com.typesafe.scalalogging.LazyLogging
import spray.json._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{FiniteDuration, _}

class ElasticSearchActor(
                          implicit
                          system: ActorSystem,
                          ec: ExecutionContext,
                          timeout: Timeout
                        ) extends Actor with LazyLogging {

  val retryStep = 10
  val retryMax = 5
  var offset = 0

  self ! Start()

  override def receive(): Receive = {

    case Start() =>
      logger.info(s"""start elastic search actor""")

    case SendIndexToElasticSearchActor(index, url) =>

      ElasticSearchApi.post(index, ElasticConfig.postIndex + url)
        .onComplete {
          case scala.util.Success(r) =>
            val (status, body) = r
            if (status == StatusCodes.Created || status == StatusCodes.OK) {

            } else {
              self ! retry(10.second, SendErrorToElasticSearchActor(
                ElasticSearchError(
                  s"""response send elastic search service with error status: $status, body : $body""", DateTimeUtils.now).toJson,
                s"""/error_idx/error""")
              )
            }
          case scala.util.Failure(error) =>
            logger.info(s"""response send elastic search service with error: ${error.getMessage}""")
            self ! retry(10.second, SendErrorToElasticSearchActor(
              ElasticSearchError(
                s"""response send elastic search service with error: ${error.getMessage}""", DateTimeUtils.now).toJson,
              s"""/error_idx/error""")
            )
        }

    case SendErrorToElasticSearchActor(error, url) =>

      ElasticSearchApi.post(error, ElasticConfig.postIndex + url)

    case _ =>
      logger.info(s"""welcome to elastic search actor""")

  }

  def retry(time: FiniteDuration, message: SendErrorToElasticSearchActor): Cancellable = {
    context.system.scheduler.scheduleOnce(time, self, message)
  }

}

object ElasticSearchActor {

  case class GeoPoint(text: String, location: String)

  case class ElasticUserTracking(
                                  id: Option[Long] = None,
                                  userId: Long,
                                  text: String,
                                  location: String,
                                  createdAt: LocalDateTime,
                                  modifiedAt: Option[LocalDateTime] = None,
                                  disabled: Boolean = false,
                                  deleted: Boolean = false
                                )

}
