package com.bot.repos.database

import com.bot.DI._
import com.bot.repos.filter.FilterTableComponent
import com.bot.repos.guild._3net.{GuildTableComponent, SubGuildTableComponent}
import com.bot.repos.place.{LikeTableComponent, QueryPlaceTableComponent, ToiletTableComponent, UserPlaceTableComponent}
import com.bot.repos.review.{QuestionSubGuildTableComponent, UserLikeTableComponent}
import com.bot.repos.user.{UserActivityTableComponent, UserTableComponent, UserTrackingTableComponent}
import com.bot.repos.zone.ZoneTableComponent
import com.bot.utils.PostgresProfiler.api._
import com.typesafe.scalalogging.LazyLogging
import slick.jdbc.meta.MTable

import scala.concurrent.Future


object DBSetup extends UserTableComponent
  with GuildTableComponent
  with SubGuildTableComponent
  with ToiletTableComponent
  with ZoneTableComponent
  with FilterTableComponent
  with UserActivityTableComponent
  with UserTrackingTableComponent
  with UserLikeTableComponent
  with LikeTableComponent
  with QueryPlaceTableComponent
  with UserPlaceTableComponent
  with QuestionSubGuildTableComponent
  with LazyLogging {

  private val tables = List(
    userTable,
    guildTable,
    subGuildTable,
    toiletTable,
    zoneTable,
    filterTable,
    userActivityTable,
    userTrackingTable,
    userLikeTable,
    likeTable,
    queryPlaceTable,
    userPlaceTable,
    questionSubGuildTable
  )

  def initDbs(): Future[List[Unit]] = {
    logger.info(s"initiating dbs on : ${config.getString("db.url")}")
    val existing = db.run(MTable.getTables)
    val f = existing.flatMap(v => {
      val names = v.map(mt => mt.name.name)
      val createIfNotExist =
        tables.filter(table => !names.contains(table.baseTableRow.tableName))
          .map(_.schema.create)
      db.run(DBIO.sequence(createIfNotExist))
    })
    f
  }
}

