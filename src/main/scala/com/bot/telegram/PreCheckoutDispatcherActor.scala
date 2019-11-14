package com.bot.telegram

import akka.actor.{Actor, ActorSystem}
import akka.util.Timeout
import com.bot.services.telegram.TelegramBotService.Messages._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext

class PreCheckoutDispatcherActor(
                              implicit
                              system: ActorSystem,
                              ec: ExecutionContext,
                              timeout: Timeout
                            ) extends Actor with LazyLogging {

  self ! Start()

  override def receive(): Receive = {

    case Start() =>
      logger.info(s"""pre checkout dispatcher actor is started""")

    case SendPreCheckoutQueryToDispatcher(preCheckoutQuery) =>
      logger.info(s"""welcome to pre checkout dispatcher actor""")

    case _ =>
      logger.info(s"""welcome to pre checkout dispatcher actor""")

  }

}
