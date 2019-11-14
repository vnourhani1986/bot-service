package com.bot.telegram

import akka.actor.{Actor, ActorSystem, Cancellable}
import akka.http.scaladsl.model.StatusCodes
import akka.util.Timeout
import com.bot.formats.Formats._
import com.bot.models.json.bot.telegram._
import com.bot.services.telegram.TelegramBotApi
import com.bot.services.telegram.TelegramBotService.Messages._
import com.bot.services.telegram.TelegramBotService._
import com.bot.telegram.UpdaterActor.DispatchUpdates
import com.bot.utils.TelegramBotConfig
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{FiniteDuration, _}

class UpdaterActor(
                    implicit
                    system: ActorSystem,
                    ec: ExecutionContext,
                    timeout: Timeout
                  ) extends Actor with LazyLogging {

  val fetchPeriod: FiniteDuration = TelegramBotConfig.fetchPeriod.millis // millis second
  var offset = 0
  var limit: Int = TelegramBotConfig.limit

  self ! Start()

  override def receive(): Receive = {

    case Start() =>
      logger.info(s"""telegram bot updater actor is started""")
      getUpdates(fetchPeriod)

    case GetUpdates() =>

      TelegramBotApi.getUpdates(offset, limit)
        .map {
          case (status, body) if status == StatusCodes.OK =>
            val response = body.convertTo[Result]
            if (response.ok) {
              offset = if (response.result.nonEmpty) response.result.map(_.update_id).last + 1 else offset
              self ! DispatchUpdates(response)
            }
            getUpdates(fetchPeriod)
        }
        .recover {
          case error: Throwable =>
            logger.info(s"""receive updates from telegram bot service with error: ${error.getMessage}""")
            getUpdates(fetchPeriod)
        }

    case DispatchUpdates(response) =>

      response.result.foreach { update =>
        update.message.foreach(message => messageDispatcherActor ! SendMessageToDispatcher(message))
        update.edited_message.foreach(message => messageDispatcherActor ! SendEditedMessageToDispatcher(message))
        update.channel_post.foreach(message => messageDispatcherActor ! SendChannelPostToDispatcher(message))
        update.edited_channel_post.foreach(message => messageDispatcherActor ! SendEditedMessageToDispatcher(message))
        update.inline_query.foreach(inlineQuery => inlineDispatcherActor ! SendInlineQueryToDispatcher(inlineQuery))
        update.chosen_inline_result.foreach(chosenInlineResult => inlineDispatcherActor ! SendChosenInlineResultToDispatcher(chosenInlineResult))
        update.callback_query.foreach(callbackQuery => callbackDispatcherActor ! SendCallbackQueryToDispatcher(callbackQuery))
        update.shipping_query.foreach(shippingQuery => shippingDispatcherActor ! SendShippingQueryToDispatcher(shippingQuery))
        update.pre_checkout_query.foreach(preCheckoutQuery => preCheckoutDispatcherActor ! SendPreCheckoutQueryToDispatcher(preCheckoutQuery))
        update.poll.foreach(poll => pollDispatcherActor ! SendPollToDispatcher(poll))
      }

    case _ =>
      logger.info(s"""welcome to telegram get updates actor""")

  }

  def getUpdates(time: FiniteDuration): Cancellable = {
    context.system.scheduler.scheduleOnce(time, self, GetUpdates())
  }

}

object UpdaterActor {

  case class DispatchUpdates(result: Result)

}
