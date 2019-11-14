package com.bot.repos.guild.dunro

import java.time.LocalDateTime

import com.bot.DI._
import com.bot.models.repo.guild.{Guild, Parent}
import com.bot.repos.SlickSupport
import com.bot.repos.guild._3net.GuildRepoImpl
import com.bot.utils.DateTimeUtils
import com.bot.utils.PostgresProfiler.api._
import slick.lifted.ProvenShape

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

trait GuildParentRepo {

  def find(title: String): Future[Option[Parent]]

  def findById(id: Long): Future[Option[Parent]]

  def get: Future[Seq[Parent]]

  def save(guildParent: Parent): Future[Parent]

  def update(guildParent: Parent): Future[Boolean]

  def delete(id: Long): Future[Boolean]

  def isDeleted(title: String): Future[Boolean]

}

object GuildParentRepoImpl extends GuildParentRepo with GuildParentTableComponent {

  override def get: Future[Seq[Parent]] = {
    val query = guildParentTable
    db.run(query.result)
  }

  override def find(title: String): Future[Option[Parent]] = {

    val query = guildParentTable
      .filter(_.title === title)
      .result
      .headOption
    db.run(query)

  }

  override def findById(id: Long): Future[Option[Parent]] = {

    val query = guildParentTable
      .filter(_.id === id)
      .result
      .headOption
    db.run(query)

  }

  override def save(guildParent: Parent): Future[Parent] = {

    val action = guildParentTable returning guildParentTable.map(_.id) into ((table, id) =>
      table.copy(id = Some(id))) += guildParent.copy(createdAt = LocalDateTime.now)
    db.run(action)

  }

  override def update(guildParent: Parent): Future[Boolean] = {

    val action = guildParentTable
      .filter(_.id === guildParent.id)
      .update(guildParent.copy(modifiedAt = DateTimeUtils.nowOpt))
    db.run(action).map(_ > 0)

  }

  override def delete(id: Long): Future[Boolean] = {

    val action = guildParentTable
      .filter(_.id === id)
      .map(x => (x.modifiedAt, x.deleted))
      .update((DateTimeUtils.nowOpt, true))

    db.run(action).map(_ > 0)

  }

  override def isDeleted(title: String): Future[Boolean] = {

    val query = guildParentTable
      .filter(_.title === title)
      .map(_.deleted)
    db.run(query.result.head)

  }

}

private[repos] trait GuildParentTableComponent extends SlickSupport {

  private[GuildParentTableComponent] final class GuildParentTable(tag: Tag)
    extends Table[Parent](tag, "dunro_guild_parent") {

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey)

    def playerId: Rep[Option[Long]] = column[Option[Long]]("player_id")

    def title: Rep[Option[String]] = column[Option[String]]("title")

    def public: Rep[Option[Int]] = column[Option[Int]]("public")

    def icon: Rep[Option[String]] = column[Option[String]]("icon")

    def version: Rep[Option[Double]] = column[Option[Double]]("version")

    def template: Rep[Option[String]] = column[Option[String]]("template")

    def equality: Rep[Option[Int]] = column[Option[Int]]("equality")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")

    def modifiedAt: Rep[Option[LocalDateTime]] = column[Option[LocalDateTime]]("modified_at")

    def deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))

    def * : ProvenShape[Parent] = (
      id.?,
      playerId,
      title,
      public,
      icon,
      version,
      template,
      equality,
      createdAt,
      modifiedAt,
      deleted) <> ((Parent.apply _).tupled, Parent.unapply)
  }

  protected val guildParentTable = TableQuery[GuildParentTable]

}