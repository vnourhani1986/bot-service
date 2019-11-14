package com.bot.repos.guild._3net

import java.time.LocalDateTime

import com.bot.DI._
import com.bot.models.repo.guild.Guild
import com.bot.repos.SlickSupport
import com.bot.utils.DateTimeUtils
import com.bot.utils.PostgresProfiler.api._
import slick.lifted.ProvenShape

import scala.concurrent.Future

trait GuildRepo {

  def find(title: String): Future[Option[Guild]]

  def findById(id: Long): Future[Option[Guild]]

  def findByQuery(query: String): Future[Seq[Guild]]

  def get: Future[Seq[Guild]]

  def save(guild: Guild): Future[Guild]

  def update(guild: Guild): Future[Boolean]

  def delete(id: Long): Future[Boolean]

  def isDeleted(title: String): Future[Boolean]

}

object GuildRepoImpl extends GuildRepo with GuildTableComponent {

  override def get: Future[Seq[Guild]] = {
    val query = guildTable
        .filter(!_.disabled)
        .filter(!_.deleted)
        .sortBy(_.title)
    db.run(query.result)
  }

  override def find(title: String): Future[Option[Guild]] = {

    val query = guildTable
      .filter(_.title === title)
      .filter(!_.disabled)
      .filter(!_.deleted)
      .result
      .headOption
    db.run(query)

  }

  override def findByQuery(qTitle: String): Future[Seq[Guild]] = {

    val query = guildTable
      .filter(_.title like s"""%$qTitle%""")
      .filter(!_.disabled)
      .filter(!_.deleted)
      .sortBy(_.title)
      .take(5)
      .result
    db.run(query)

  }

  override def findById(id: Long): Future[Option[Guild]] = {

    val query = guildTable
      .filter(_.id === id)
      .filter(!_.disabled)
      .filter(!_.deleted)
      .result
      .headOption
    db.run(query)

  }

  override def save(guild: Guild): Future[Guild] = {

    val action = guildTable returning guildTable.map(_.id) into ((table, id) =>
      table.copy(id = Some(id))) += guild.copy(createdAt = LocalDateTime.now)
    db.run(action)

  }

  override def update(guild: Guild): Future[Boolean] = {

    val action = guildTable
      .filter(_.id === guild.id)
      .update(guild.copy(modifiedAt = DateTimeUtils.nowOpt))
    db.run(action).map(_ > 0)

  }

  override def delete(id: Long): Future[Boolean] = {

    val action = guildTable
      .filter(_.id === id)
      .map(x => (x.modifiedAt, x.deleted))
      .update((DateTimeUtils.nowOpt, true))

    db.run(action).map(_ > 0)

  }

  override def isDeleted(title: String): Future[Boolean] = {

    val query = guildTable
      .filter(_.title === title)
      .map(_.deleted)
    db.run(query.result.head)

  }

}

private[repos] trait GuildTableComponent extends SlickSupport {

  private[GuildTableComponent] final class GuildTable(tag: Tag)
    extends Table[Guild](tag, "guild") {

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

    def disabled: Rep[Boolean] = column[Boolean]("disabled", O.Default(true))

    def deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))

    def * : ProvenShape[Guild] = (
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
      disabled,
      deleted) <> ((Guild.apply _).tupled, Guild.unapply)
  }

  protected val guildTable = TableQuery[GuildTable]

}