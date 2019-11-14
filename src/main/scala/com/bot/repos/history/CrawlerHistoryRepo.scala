package com.bot.repos.history

import java.time.LocalDateTime

import com.bot.DI._
import com.bot.models.repo.history.CrawlerHistory
import com.bot.repos.SlickSupport
import com.bot.utils.DateTimeUtils
import com.bot.utils.PostgresProfiler.api._
import slick.lifted.ProvenShape

import scala.concurrent.Future

trait CrawlerHistoryRepo {

  def find(cityId: Long): Future[Option[CrawlerHistory]]

  def get: Future[Seq[CrawlerHistory]]

  def getLastLog: Future[Option[CrawlerHistory]]

  def save(crawlerHistory: CrawlerHistory): Future[CrawlerHistory]

  def update(crawlerHistory: CrawlerHistory): Future[Boolean]

  def delete(id: Long): Future[Boolean]

  def isDeleted(cityId: Long): Future[Boolean]

}

object CrawlerHistoryRepoImpl extends CrawlerHistoryRepo with CrawlerHistoryTableComponent {

  override def get: Future[Seq[CrawlerHistory]] = {
    val query = crawlerHistoryTable
    db.run(query.result)
  }

  override def getLastLog: Future[Option[CrawlerHistory]] = {
    val query = crawlerHistoryTable
      .sortBy(_.createdAt.desc)
      .result
      .headOption
    db.run(query)
  }

  override def find(cityId: Long): Future[Option[CrawlerHistory]] = {

    val query = crawlerHistoryTable
      .filter(_.cityId === cityId)
      .result
      .headOption
    db.run(query)

  }

  override def save(crawlerHistory: CrawlerHistory): Future[CrawlerHistory] = {

    val action = crawlerHistoryTable returning crawlerHistoryTable.map(_.id) into ((table, id) =>
      table.copy(id = Some(id))) += crawlerHistory.copy(createdAt = LocalDateTime.now)
    db.run(action)

  }

  override def update(crawlerHistory: CrawlerHistory): Future[Boolean] = {

    val action = crawlerHistoryTable
      .filter(_.id === crawlerHistory.id)
      .update(crawlerHistory.copy(modifiedAt = DateTimeUtils.nowOpt))

    db.run(action).map(_ > 0)

  }

  override def delete(id: Long): Future[Boolean] = {

    val action = crawlerHistoryTable
      .filter(_.id === id)
      .map(x => (x.modifiedAt, x.deleted))
      .update((DateTimeUtils.nowOpt, true))

    db.run(action).map(_ > 0)

  }

  override def isDeleted(cityId: Long): Future[Boolean] = {

    val query = crawlerHistoryTable
      .filter(_.cityId === cityId)
      .map(_.deleted)

    db.run(query.result.head)

  }

}

private[repos] trait CrawlerHistoryTableComponent extends SlickSupport {

  private[CrawlerHistoryTableComponent] final class CrawlerHistoryTable(tag: Tag)
    extends Table[CrawlerHistory](tag, "crawler_history") {

    def id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def cityId: Rep[Option[Long]] = column[Option[Long]]("city_id")

    def placeId: Rep[Option[Long]] = column[Option[Long]]("place_id")

    def childGuildId: Rep[Option[Long]] = column[Option[Long]]("child_guild_id")

    def parentGuildId: Rep[Option[Long]] = column[Option[Long]]("parent_guild_id")

    def cityName: Rep[Option[String]] = column[Option[String]]("city_name")

    def placeName: Rep[Option[String]] = column[Option[String]]("place_name")

    def childGuildName: Rep[Option[String]] = column[Option[String]]("child_guild_name")

    def parentGuildName: Rep[Option[String]] = column[Option[String]]("parent_guild_name")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")

    def modifiedAt: Rep[Option[LocalDateTime]] = column[Option[LocalDateTime]]("modified_at")

    def deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))

    def * : ProvenShape[CrawlerHistory] = (
      id.?,
      cityId,
      placeId,
      childGuildId,
      parentGuildId,
      cityName,
      placeName,
      childGuildName,
      parentGuildName,
      createdAt,
      modifiedAt,
      deleted) <> ((CrawlerHistory.apply _).tupled, CrawlerHistory.unapply)
  }

  protected val crawlerHistoryTable = TableQuery[CrawlerHistoryTable]

}