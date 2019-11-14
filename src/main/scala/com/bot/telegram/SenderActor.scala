package com.bot.telegram

import akka.actor.{Actor, ActorSystem, Cancellable}
import akka.http.scaladsl.model.StatusCodes
import akka.util.Timeout
import com.bot.formats.Formats._
import com.bot.models.json.bot.telegram._
import com.bot.services.telegram.TelegramBotApi
import com.bot.services.telegram.TelegramBotService.Messages._
import com.bot.services.telegram.TelegramBotService.elasticSearchActor
import com.bot.utils.{DateTimeUtils, TelegramBotConfig}
import com.typesafe.scalalogging.LazyLogging
import spray.json._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{FiniteDuration, _}

class SenderActor(
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
      logger.info(s"""start telegram bot send actor""")

    case message: SendMessage =>

      TelegramBotApi.send(message.toJson, TelegramBotConfig.sendMessageUrl)
        .onComplete {
          case scala.util.Success(r) =>
            val (status, body) = r
            if (status == StatusCodes.OK || status == StatusCodes.BadRequest) {

            } else {
              self ! retry(1.second, message)
            }
          case scala.util.Failure(error) =>
            logger.info(s"""response send message bot service that need to send again because of error: ${error.getMessage}""")
            elasticSearchActor ! SendErrorToElasticSearchActor(
              ElasticSearchError(
                s"""response send message bot service that need to send again because of error: ${error.getMessage}""", DateTimeUtils.now).toJson,
              s"""/error_idx/error""")
            self ! retry(1.second, message)
        }

    case message: SendMessage1 =>

      TelegramBotApi.send(message.toJson, TelegramBotConfig.sendMessageUrl)
        .onComplete {
          case scala.util.Success(r) =>
            val (status, body) = r
            if (status == StatusCodes.OK || status == StatusCodes.BadRequest) {

            } else {
              self ! retry(1.second, message)
            }
          case scala.util.Failure(error) =>
            logger.info(s"""response send message bot service that need to send again because of error: ${error.getMessage}""")
            elasticSearchActor ! SendErrorToElasticSearchActor(
              ElasticSearchError(
                s"""response send message bot service that need to send again because of error: ${error.getMessage}""", DateTimeUtils.now).toJson,
              s"""/error_idx/error""")
            self ! retry(1.second, message)
        }

    case message: SendLocation =>

      TelegramBotApi.send(message.toJson, TelegramBotConfig.sendLocationUrl)
        .onComplete {
          case scala.util.Success(r) =>
            val (status, body) = r
            if (status == StatusCodes.OK || status == StatusCodes.BadRequest) {

            } else {
              self ! retry(1.second, message)
            }
          case scala.util.Failure(error) =>
            logger.info(s"""response send message bot service that need to send again because of error: ${error.getMessage}""")
            elasticSearchActor ! SendErrorToElasticSearchActor(
              ElasticSearchError(
                s"""response send message bot service that need to send again because of error: ${error.getMessage}""", DateTimeUtils.now).toJson,
              s"""/error_idx/error""")
            self ! retry(1.second, message)
        }

    case message: SendVenue =>

      TelegramBotApi.send(message.toJson, TelegramBotConfig.sendVenueUrl)
        .onComplete {
          case scala.util.Success(r) =>
            val (status, body) = r
            if (status == StatusCodes.OK || status == StatusCodes.BadRequest) {
            } else {
              self ! retry(1.second, message)
            }
          case scala.util.Failure(error) =>
            logger.info(s"""response send message bot service that need to send again because of error: ${error.getMessage}""")
            elasticSearchActor ! SendErrorToElasticSearchActor(
              ElasticSearchError(
                s"""response send message bot service that need to send again because of error: ${error.getMessage}""", DateTimeUtils.now).toJson,
              s"""/error_idx/error""")
            self ! retry(1.second, message)
        }

    case message: SendPhoto =>

      TelegramBotApi.send(message.toJson, TelegramBotConfig.sendPhotoUrl)
        .onComplete {
          case scala.util.Success(r) =>
            val (status, _) = r
            if (status == StatusCodes.OK || status == StatusCodes.BadRequest) {

            } else {
              self ! retry(1.second, message)
            }
          case scala.util.Failure(error) =>
            logger.info(s"""response send message bot service that need to send again because of error: ${error.getMessage}""")
            elasticSearchActor ! SendErrorToElasticSearchActor(
              ElasticSearchError(
                s"""response send message bot service that need to send again because of error: ${error.getMessage}""", DateTimeUtils.now).toJson,
              s"""/error_idx/error""")
            self ! retry(1.second, message)
        }

    case message: SendPhoto1 =>

      TelegramBotApi.send(message.toJson, TelegramBotConfig.sendPhotoUrl)
        .onComplete {
          case scala.util.Success(r) =>
            val (status, _) = r
            if (status == StatusCodes.OK || status == StatusCodes.BadRequest) {

            } else {
              self ! retry(1.second, message)
            }
          case scala.util.Failure(error) =>
            logger.info(s"""response send message bot service that need to send again because of error: ${error.getMessage}""")
            elasticSearchActor ! SendErrorToElasticSearchActor(
              ElasticSearchError(
                s"""response send message bot service that need to send again because of error: ${error.getMessage}""", DateTimeUtils.now).toJson,
              s"""/error_idx/error""")
            self ! retry(1.second, message)
        }

    case message: DeleteMessage =>

      TelegramBotApi.send(message.toJson, TelegramBotConfig.deleteMessageUrl)
        .onComplete {
          case scala.util.Success(r) =>
            val (status, _) = r
            if (status == StatusCodes.OK || status == StatusCodes.BadRequest) {

            } else {
              self ! retry(1.second, message)
            }
          case scala.util.Failure(error) =>
            logger.info(s"""response send message bot service that need to send again because of error: ${error.getMessage}""")
            elasticSearchActor ! SendErrorToElasticSearchActor(
              ElasticSearchError(
                s"""response send message bot service that need to send again because of error: ${error.getMessage}""", DateTimeUtils.now).toJson,
              s"""/error_idx/error""")
            self ! retry(1.second, message)
        }

    case message: EditMessageCaption =>

      TelegramBotApi.send(message.toJson, TelegramBotConfig.editMessageCaptionUrl)
        .onComplete {
          case scala.util.Success(r) =>
            val (status, _) = r
            if (status == StatusCodes.OK || status == StatusCodes.BadRequest) {

            } else {
              self ! retry(1.second, message)
            }
          case scala.util.Failure(error) =>
            logger.info(s"""response send message bot service that need to send again because of error: ${error.getMessage}""")
            elasticSearchActor ! SendErrorToElasticSearchActor(
              ElasticSearchError(
                s"""response send message bot service that need to send again because of error: ${error.getMessage}""", DateTimeUtils.now).toJson,
              s"""/error_idx/error""")
            self ! retry(1.second, message)
        }

    case answerQuery: AnswerCallbackQuery =>

      TelegramBotApi.send(answerQuery.toJson, TelegramBotConfig.sendAnswerCallbackQueryUrl)
        .onComplete {
          case scala.util.Success(r) =>
            val (status, body) = r
            if (status == StatusCodes.OK || status == StatusCodes.BadRequest) {

            } else {
              self ! retry(1.second, answerQuery)
            }
          case scala.util.Failure(error) =>
            logger.info(s"""response send message bot service that need to send again because of error: ${error.getMessage}""")
            elasticSearchActor ! SendErrorToElasticSearchActor(
              ElasticSearchError(
                s"""response send message bot service that need to send again because of error: ${error.getMessage}""", DateTimeUtils.now).toJson,
              s"""/error_idx/error""")
            self ! retry(1.second, answerQuery)
        }

    case answerQuery: AnswerInlineQueryVenue =>

      TelegramBotApi.send(answerQuery.toJson, TelegramBotConfig.sendAnswerInlineQueryUrl)
        .onComplete {
          case scala.util.Success(r) =>
            val (status, body) = r
            if (status == StatusCodes.OK || status == StatusCodes.BadRequest) {

            } else {
              self ! retry(1.second, answerQuery)
            }
          case scala.util.Failure(error) =>
            logger.info(s"""response send message bot service that need to send again because of error: ${error.getMessage}""")
            elasticSearchActor ! SendErrorToElasticSearchActor(
              ElasticSearchError(
                s"""response send message bot service that need to send again because of error: ${error.getMessage}""", DateTimeUtils.now).toJson,
              s"""/error_idx/error""")
            self ! retry(1.second, answerQuery)
        }

    case answerQuery: AnswerInlineQueryArticle =>

      TelegramBotApi.send(answerQuery.toJson, TelegramBotConfig.sendAnswerInlineQueryUrl)
        .onComplete {
          case scala.util.Success(r) =>
            val (status, body) = r
            if (status == StatusCodes.OK || status == StatusCodes.BadRequest) {

            } else {
              self ! retry(1.second, answerQuery)
            }
          case scala.util.Failure(error) =>
            logger.info(s"""response send message bot service that need to send again because of error: ${error.getMessage}""")
            elasticSearchActor ! SendErrorToElasticSearchActor(
              ElasticSearchError(
                s"""response send message bot service that need to send again because of error: ${error.getMessage}""", DateTimeUtils.now).toJson,
              s"""/error_idx/error""")
            self ! retry(1.second, answerQuery)
        }

    case _ =>
      logger.info(s"""welcome to telegram send actor""")

  }


  def getUpdates(time: FiniteDuration): Cancellable = {
    context.system.scheduler.scheduleOnce(time, self, GetUpdates())
  }

  def retry(time: FiniteDuration, message: SendMessage): Cancellable = {
    context.system.scheduler.scheduleOnce(time, self, message)
  }

  def retry(time: FiniteDuration, message: SendMessage1): Cancellable = {
    context.system.scheduler.scheduleOnce(time, self, message)
  }

  def retry(time: FiniteDuration, message: SendLocation): Cancellable = {
    context.system.scheduler.scheduleOnce(time, self, message)
  }

  def retry(time: FiniteDuration, message: SendVenue): Cancellable = {
    context.system.scheduler.scheduleOnce(time, self, message)
  }

  def retry(time: FiniteDuration, message: SendPhoto): Cancellable = {
    context.system.scheduler.scheduleOnce(time, self, message)
  }

  def retry(time: FiniteDuration, message: SendPhoto1): Cancellable = {
    context.system.scheduler.scheduleOnce(time, self, message)
  }

  def retry(time: FiniteDuration, message: DeleteMessage): Cancellable = {
    context.system.scheduler.scheduleOnce(time, self, message)
  }

  def retry(time: FiniteDuration, message: EditMessageCaption): Cancellable = {
    context.system.scheduler.scheduleOnce(time, self, message)
  }

  def retry(time: FiniteDuration, message: AnswerCallbackQuery): Cancellable = {
    context.system.scheduler.scheduleOnce(time, self, message)
  }

  def retry(time: FiniteDuration, message: AnswerInlineQueryVenue): Cancellable = {
    context.system.scheduler.scheduleOnce(time, self, message)
  }

  def retry(time: FiniteDuration, message: AnswerInlineQueryArticle): Cancellable = {
    context.system.scheduler.scheduleOnce(time, self, message)
  }


}