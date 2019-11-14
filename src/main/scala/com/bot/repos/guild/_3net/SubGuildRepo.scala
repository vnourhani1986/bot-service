package com.bot.repos.guild._3net

import java.time.LocalDateTime

import com.bot.DI._
import com.bot.models.repo.guild.SubGuild
import com.bot.repos.SlickSupport
import com.bot.utils.DateTimeUtils
import com.bot.utils.PostgresProfiler.api._
import slick.lifted.ProvenShape

import scala.concurrent.Future

trait SubGuildRepo {

  def find(title: String): Future[Option[SubGuild]]

  def findByQuery(query: String): Future[Seq[SubGuild]]

  def findByQuery(qTitle: String, take: Int): Future[Seq[SubGuild]]

  def find(guildId: Long): Future[Seq[SubGuild]]

  def find(guildIds: List[Long]): Future[Seq[SubGuild]]

  def findNextById(id: Long): Future[(Option[SubGuild], Boolean)]

  def get: Future[Seq[SubGuild]]

  def save(subGuild: SubGuild): Future[SubGuild]

  def update(subGuild: SubGuild): Future[Boolean]

  def delete(id: Long): Future[Boolean]

  def isDeleted(title: String): Future[Boolean]

}

object SubGuildRepoImpl extends SubGuildRepo with SubGuildTableComponent {

  override def get: Future[Seq[SubGuild]] = {
    val query = subGuildTable
      .filter(!_.disabled)
      .filter(!_.deleted)
      .sortBy(_.title)
    db.run(query.result)
  }

  override def find(title: String): Future[Option[SubGuild]] = {

    val query = subGuildTable
      .filter(_.title === title)
      .filter(!_.disabled)
      .filter(!_.deleted)
      .result
      .headOption
    db.run(query)

  }

  override def findByQuery(q: String): Future[Seq[SubGuild]] = {

    val query = subGuildTable
      .filter(_.title like s"""%$q%""")
      .filter(!_.disabled)
      .filter(!_.deleted)
      .sortBy(_.title)
      .result
    db.run(query)

  }

  override def findByQuery(qTitle: String, take: Int): Future[Seq[SubGuild]] = {

    val query = subGuildTable
      .filter(_.title like s"""%$qTitle%""")
      .filter(!_.disabled)
      .filter(!_.deleted)
      .take(take)
      .result
    db.run(query)

  }

  override def find(guildId: Long): Future[Seq[SubGuild]] = {

    val query = subGuildTable
      .filter(_.guildId === guildId)
      .filter(!_.disabled)
      .filter(!_.deleted)
      .sortBy(_.title)
      .result
    db.run(query)

  }

  override def find(guildIds: List[Long]): Future[Seq[SubGuild]] = {

    val query = subGuildTable
      .filter(_.id inSet guildIds)
      .filter(!_.disabled)
      .filter(!_.deleted)
      .sortBy(_.title)
      .result
    db.run(query)

  }

  override def findNextById(id: Long): Future[(Option[SubGuild], Boolean)] = {

    val query = subGuildTable
      .filter(table => table.id > id)
      .sortBy(_.id)
      .result
      .headOption
    db.run(query).flatMap { nCity =>
      if (nCity.isDefined) Future.successful((nCity, false)) else {
        val q = subGuildTable
          .sortBy(_.id)
          .result
          .headOption
        db.run(q).map(x => (x, true)) // true -> go to first guild in table and need to reset
      }
    }

  }

  override def save(subGuild: SubGuild): Future[SubGuild] = {

    val action = subGuildTable returning subGuildTable.map(_.id) into ((table, id) =>
      table.copy(id = Some(id))) += subGuild.copy(createdAt = LocalDateTime.now)
    db.run(action)

  }

  override def update(subGuild: SubGuild): Future[Boolean] = {

    val action = subGuildTable
      .filter(_.id === subGuild.id)
      .update(subGuild.copy(modifiedAt = DateTimeUtils.nowOpt))
    db.run(action).map(_ > 0)

  }

  override def delete(id: Long): Future[Boolean] = {

    val action = subGuildTable
      .filter(_.id === id)
      .map(x => (x.modifiedAt, x.deleted))
      .update((DateTimeUtils.nowOpt, true))

    db.run(action).map(_ > 0)

  }

  override def isDeleted(title: String): Future[Boolean] = {

    val query = subGuildTable
      .filter(_.title === title)
      .map(_.deleted)
    db.run(query.result.head)

  }

}

private[repos] trait SubGuildTableComponent extends SlickSupport {

  private[SubGuildTableComponent] final class SubGuildTable(tag: Tag)
    extends Table[SubGuild](tag, "sub_guild") {

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey)

    def playerId: Rep[Option[Long]] = column[Option[Long]]("player_id")

    def guildId: Rep[Option[Long]] = column[Option[Long]]("guild_id")

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

    def * : ProvenShape[SubGuild] = (
      id.?,
      playerId,
      title,
      guildId,
      public,
      icon,
      version,
      template,
      equality,
      createdAt,
      modifiedAt,
      disabled,
      deleted) <> ((SubGuild.apply _).tupled, SubGuild.unapply)
  }

  protected val subGuildTable = TableQuery[SubGuildTable]

}