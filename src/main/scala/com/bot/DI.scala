package com.bot

import java.time.ZoneId

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.slick.scaladsl.SlickSession
import com.bot.utils.PostgresProfiler.api._
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContextExecutor

object DI {
  implicit val config: Config = ConfigFactory.load()
  implicit val system: ActorSystem = ActorSystem("bot")
  implicit val ec: ExecutionContextExecutor = system.dispatcher //bindAndHandle requires an implicit ExecutionContext
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val timezone: ZoneId = ZoneId.of(config.getString("timezone"))
  implicit val db: Database = com.bot.repos.database.DBManager.db
  implicit val dbSession: SlickSession = SlickSession.forConfig("slick-postgres")
}