package com.bot.telegram

import akka.actor.{Actor, ActorSystem}
import akka.util.Timeout
import com.bot.services.telegram.TelegramBotService.Messages._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext

class GEOCalcActor(
                    implicit
                    system: ActorSystem,
                    ec: ExecutionContext,
                    timeout: Timeout
                  ) extends Actor with LazyLogging {

  self ! Start()

  override def receive(): Receive = {

    case Start() =>
      logger.info(s"""geo calculator dispatcher actor is started""")

    case _ =>
      logger.info(s"""welcome to geo calculator dispatcher actor""")

  }

}
