package com.bot.telegram

import akka.actor.{Actor, ActorSystem}
import akka.util.Timeout
import com.bot.services.osrm.OsrmApi
import com.bot.services.telegram.TelegramBotService.Messages._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext

class OSRMActor(
                 implicit
                 system: ActorSystem,
                 ec: ExecutionContext,
                 timeout: Timeout
               ) extends Actor with LazyLogging {

  self ! Start()

  override def receive(): Receive = {

    case Start() =>
      logger.info(s"""osrm dispatcher actor is started""")

    case GetDuration(source, destination) =>

      val ref = sender()
      OsrmApi.getDurations(source, destination).map { r =>
        ref ! r
      }

    case _ =>
      logger.info(s"""welcome to osrm dispatcher actor""")

  }

}
