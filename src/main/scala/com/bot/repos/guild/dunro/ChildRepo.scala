package com.bot.repos.guild.dunro

import java.time.LocalDateTime

import com.bot.DI._
import com.bot.models.repo.guild.{Child, Guild, SubGuild}
import com.bot.repos.SlickSupport
import com.bot.repos.guild._3net.{GuildRepoImpl, SubGuildRepoImpl}
import com.bot.utils.DateTimeUtils
import com.bot.utils.PostgresProfiler.api._
import slick.lifted.ProvenShape

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

trait GuildChildRepo {

  def find(title: String): Future[Option[Child]]

  def findNextById(id: Long): Future[(Option[Child], Boolean)]

  def get: Future[Seq[Child]]

  def save(guildChild: Child): Future[Child]

  def update(guildChild: Child): Future[Boolean]

  def delete(id: Long): Future[Boolean]

  def isDeleted(title: String): Future[Boolean]

}

object GuildChildRepoImpl extends GuildChildRepo with GuildChildTableComponent {

  override def get: Future[Seq[Child]] = {
    val query = guildChildTable
    db.run(query.result)
  }

  override def find(title: String): Future[Option[Child]] = {

    val query = guildChildTable
      .filter(_.title === title)
      .result
      .headOption
    db.run(query)

  }

  override def findNextById(id: Long): Future[(Option[Child], Boolean)] = {

    val query = guildChildTable
      .filter(table => table.id > id)
      .sortBy(_.id)
      .result
      .headOption
    db.run(query).flatMap { nCity =>
      if (nCity.isDefined) Future.successful((nCity, false)) else {
        val q = guildChildTable
          .sortBy(_.id)
          .result
          .headOption
        db.run(q).map(x => (x, true)) // true -> go to first guild in table and need to reset
      }
    }

  }

  override def save(guildChild: Child): Future[Child] = {

    val action = guildChildTable returning guildChildTable.map(_.id) into ((table, id) =>
      table.copy(id = Some(id))) += guildChild.copy(createdAt = LocalDateTime.now)
    db.run(action)

  }

  override def update(guildChild: Child): Future[Boolean] = {

    val action = guildChildTable
      .filter(_.id === guildChild.id)
      .update(guildChild.copy(modifiedAt = DateTimeUtils.nowOpt))
    db.run(action).map(_ > 0)

  }

  override def delete(id: Long): Future[Boolean] = {

    val action = guildChildTable
      .filter(_.id === id)
      .map(x => (x.modifiedAt, x.deleted))
      .update((DateTimeUtils.nowOpt, true))

    db.run(action).map(_ > 0)

  }

  override def isDeleted(title: String): Future[Boolean] = {

    val query = guildChildTable
      .filter(_.title === title)
      .map(_.deleted)
    db.run(query.result.head)

  }

}

private[repos] trait GuildChildTableComponent extends SlickSupport {

  private[GuildChildTableComponent] final class GuildChildTable(tag: Tag)
    extends Table[Child](tag, "dunro_guild_child") {

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey)

    def playerId: Rep[Option[Long]] = column[Option[Long]]("player_id")

    def parentId: Rep[Option[Long]] = column[Option[Long]]("parent_id")

    def title: Rep[Option[String]] = column[Option[String]]("title")

    def public: Rep[Option[Int]] = column[Option[Int]]("public")

    def icon: Rep[Option[String]] = column[Option[String]]("icon")

    def version: Rep[Option[Double]] = column[Option[Double]]("version")

    def template: Rep[Option[String]] = column[Option[String]]("template")

    def equality: Rep[Option[Int]] = column[Option[Int]]("equality")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")

    def modifiedAt: Rep[Option[LocalDateTime]] = column[Option[LocalDateTime]]("modified_at")

    def deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))


    def * : ProvenShape[Child] = (
      id.?,
      playerId,
      title,
      parentId,
      public,
      icon,
      version,
      template,
      equality,
      createdAt,
      modifiedAt,
      deleted) <> ((Child.apply _).tupled, Child.unapply)
  }

  protected val guildChildTable = TableQuery[GuildChildTable]

}