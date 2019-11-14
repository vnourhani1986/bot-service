package com.bot.repos.city

import java.time.LocalDateTime

import com.bot.DI._
import com.bot.models.repo.city.City
import com.bot.repos.SlickSupport
import com.bot.utils.DateTimeUtils
import com.bot.utils.PostgresProfiler.api._
import slick.lifted.ProvenShape

import scala.concurrent.Future

trait CityRepo {

  def findById(id: Long): Future[Option[City]]

  def findNextById(id: Long): Future[(Option[City], Boolean)]

  def find(query: String): Future[Seq[City]]

  def findFaName(faName: String): Future[Option[City]]

  def findEnName(enName: String): Future[Option[City]]

  def get: Future[Seq[City]]

  def save(city: City): Future[City]

  def update(city: City): Future[Boolean]

  def delete(id: Long): Future[Boolean]

  def isDeleted(faName: String): Future[Boolean]

}

object CityRepoImpl extends CityRepo with CityTableComponent {

  override def get: Future[Seq[City]] = {
    val query = cityTable
    db.run(query.result)
  }

  override def findById(id: Long): Future[Option[City]] = {

    val query = cityTable
      .filter(_.id === id)
      .result
      .headOption
    db.run(query)

  }

  override def findNextById(id: Long): Future[(Option[City], Boolean)] = {

    val query = cityTable
      .filter(table => table.id > id)
      .sortBy(_.id)
      .result
      .headOption
    db.run(query).flatMap { nCity =>
      if (nCity.isDefined) Future.successful((nCity, false)) else {
        val q = cityTable
          .sortBy(_.id)
          .result
          .headOption
        db.run(q).map(x => (x, true)) // true -> go to first city in table and need to reset
      }
    }

  }

  override def find(q: String): Future[Seq[City]] = {

    val query = cityTable
      .filter(_.faName like s"""%$q%""")
      .result
    db.run(query)

  }

  override def findFaName(faName: String): Future[Option[City]] = {

    val query = cityTable
      .filter(_.faName === faName)
      .result
      .headOption
    db.run(query)

  }

  override def findEnName(enName: String): Future[Option[City]] = {

    val query = cityTable
      .filter(_.enName === enName)
      .result
      .headOption
    db.run(query)

  }

  override def save(city: City): Future[City] = {

    val action = cityTable returning cityTable.map(_.id) into ((table, id) =>
      table.copy(id = Some(id))) += City(None, city.faName, city.enName,
      DateTimeUtils.now, None)
    db.run(action)

  }

  override def update(city: City): Future[Boolean] = {

    val action = cityTable
      .filter(_.id === city.id)
      .map(x => (x.faName, x.enName, x.modifiedAt))
      .update((city.faName, city.enName, DateTimeUtils.nowOpt))

    db.run(action).map(_ > 0)

  }

  override def delete(id: Long): Future[Boolean] = {

    val action = cityTable
      .filter(_.id === id)
      .map(x => (x.modifiedAt, x.deleted))
      .update((DateTimeUtils.nowOpt, true))

    db.run(action).map(_ > 0)

  }

  override def isDeleted(faName: String): Future[Boolean] = {

    val query = cityTable
      .filter(_.faName === faName)
      .map(_.deleted)

    db.run(query.result.head)

  }

}

private[repos] trait CityTableComponent extends SlickSupport {

  private[CityTableComponent] final class CityTable(tag: Tag)
    extends Table[City](tag, "city") {

    def id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def faName: Rep[String] = column[String]("fa_name")

    def enName: Rep[Option[String]] = column[Option[String]]("en_name")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")

    def modifiedAt: Rep[Option[LocalDateTime]] = column[Option[LocalDateTime]]("modified_at")

    def deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))

    def * : ProvenShape[City] = (
      id.?,
      faName,
      enName,
      createdAt,
      modifiedAt,
      deleted) <> ((City.apply _).tupled, City.unapply)
  }

  protected val cityTable = TableQuery[CityTable]

}