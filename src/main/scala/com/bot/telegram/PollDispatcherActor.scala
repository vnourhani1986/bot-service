package com.bot.telegram

import akka.actor.{Actor, ActorSystem}
import akka.util.Timeout
import com.bot.services.telegram.TelegramBotService.Messages._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext

class PollDispatcherActor(
                              implicit
                              system: ActorSystem,
                              ec: ExecutionContext,
                              timeout: Timeout
                            ) extends Actor with LazyLogging {

  self ! Start()

  override def receive(): Receive = {

    case Start() =>
      logger.info(s"""poll dispatcher actor is started""")

    case SendPollToDispatcher(poll) =>
      logger.info(s"""welcome to poll dispatcher actor""")

    case _ =>
      logger.info(s"""welcome to poll dispatcher actor""")

  }

}
