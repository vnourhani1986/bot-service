package com.bot.telegram

import akka.actor.{Actor, ActorSystem}
import akka.util.Timeout
import com.bot.services.telegram.TelegramBotService.Messages._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext

class ShippingDispatcherActor(
                              implicit
                              system: ActorSystem,
                              ec: ExecutionContext,
                              timeout: Timeout
                            ) extends Actor with LazyLogging {

  self ! Start()

  override def receive(): Receive = {

    case Start() =>
      logger.info(s"""shipping dispatcher actor is started""")

    case SendShippingQueryToDispatcher(shippingQuery) =>
      logger.info(s"""welcome to shipping dispatcher actor""")

    case _ =>
      logger.info(s"""welcome to shipping dispatcher actor""")

  }

}
