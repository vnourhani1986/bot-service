package com.bot.repos.place

import java.time.LocalDateTime

import com.bot.DI._
import com.bot.models.repo.place.Rate
import com.bot.repos.SlickSupport
import com.bot.utils.DateTimeUtils
import com.bot.utils.PostgresProfiler.api._
import slick.lifted.ProvenShape

import scala.concurrent.Future

trait RateRepo {

  def find(placeId: Long): Future[Option[Rate]]

  def get: Future[Seq[Rate]]

  def save(rate: Rate): Future[Option[Long]]

  def update(rate: Rate): Future[Option[Long]]

  def delete(id: Long): Future[Boolean]

  def isDeleted(id: Long): Future[Boolean]

}

object RateRepoImpl extends RateRepo with RateTableComponent {

  override def get: Future[Seq[Rate]] = {
    val query = rateTable
    db.run(query.result)
  }

  override def find(placeId: Long): Future[Option[Rate]] = {

    val query = rateTable
      .filter(_.placeId === placeId)
      .result
      .headOption
    db.run(query)

  }

  override def save(rate: Rate): Future[Option[Long]] = {

    val action = rateTable returning rateTable.map(_.id) into ((table, id) =>
      table.copy(id = Some(id))) += rate.copy(createdAt = LocalDateTime.now)
    db.run(action).map(_.id)

  }

  override def update(rate: Rate): Future[Option[Long]] = {

    val action = rateTable
      .filter(_.id === rate.id)
      .update(rate.copy(modifiedAt = DateTimeUtils.nowOpt))
    db.run(action).map(x => rate.id)

  }

  override def delete(id: Long): Future[Boolean] = {

    val action = rateTable
      .filter(_.id === id)
      .map(x => (x.modifiedAt, x.deleted))
      .update((DateTimeUtils.nowOpt, true))

    db.run(action).map(_ > 0)

  }

  override def isDeleted(id: Long): Future[Boolean] = {

    val query = rateTable
      .filter(_.id === id)
      .map(_.deleted)
    db.run(query.result.head)

  }

}

private[repos] trait RateTableComponent extends SlickSupport {

  private[RateTableComponent] final class RateTable(tag: Tag)
    extends Table[Rate](tag, "place_rate") {

    def id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def placeId: Rep[Option[Long]] = column[Option[Long]]("place_id")

    def average: Rep[Option[Double]] = column[Option[Double]]("average")

    def count: Rep[Option[Int]] = column[Option[Int]]("count")

    def provider: Rep[Option[String]] = column[Option[String]]("provider")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")

    def modifiedAt: Rep[Option[LocalDateTime]] = column[Option[LocalDateTime]]("modified_at")

    def deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))

    def * : ProvenShape[Rate] = (
      id.?,
      placeId,
      average,
      count,
      provider,
      createdAt,
      modifiedAt,
      deleted) <> ((Rate.apply _).tupled, Rate.unapply)
  }

  protected val rateTable = TableQuery[RateTable]

}