package com.bot.telegram

import java.time.LocalDateTime

import akka.actor.{Actor, ActorSystem, Cancellable}
import akka.util.Timeout
import com.bot.formats.Formats._
import com.bot.services.telegram.TelegramBotService.Messages._
import com.bot.services.telegram.TelegramBotService.elasticSearchActor
import com.bot.telegram.HealthCheckActor.{Check, Health}
import com.bot.utils.DateTimeUtils
import com.typesafe.scalalogging.LazyLogging
import spray.json._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{FiniteDuration, _}

class HealthCheckActor(
                        implicit
                        system: ActorSystem,
                        ec: ExecutionContext,
                        timeout: Timeout
                      ) extends Actor with LazyLogging {

  self ! Start()

  override def receive(): Receive = {

    case Start() =>
      logger.info(s"""health check actor is started""")
      self ! Check()

    case Check() =>

      elasticSearchActor ! SendIndexToElasticSearchActor(Health(s"""telegram bot is healthy""", DateTimeUtils.now).toJson, s"""/health_idx/health""")
      retry(10.second, Check())

    case _ =>
      logger.info(s"""welcome to health check actor""")

  }

  def retry(time: FiniteDuration, message: Check): Cancellable = {
    context.system.scheduler.scheduleOnce(time, self, message)
  }

}

object HealthCheckActor {

  case class Check()

  case class Health(message: String, createdAt: LocalDateTime)

}