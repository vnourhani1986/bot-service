package com.bot.repos.database

import com.bot.utils
import com.bot.utils.PostgresProfiler
import com.typesafe.scalalogging.LazyLogging
import com.bot.utils.PostgresProfiler.api._

object DBManager extends LazyLogging {

  lazy val db: utils.PostgresProfiler.backend.Database = connect

  private def connect: PostgresProfiler.backend.Database = {
    logger.info("connecting to db")
    Database.forConfig("db")
  }

  def closeDB(): Unit = {
    logger.info("closing db connections")
    db.close()
  }

}
