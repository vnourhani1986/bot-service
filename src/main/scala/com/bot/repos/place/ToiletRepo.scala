package com.bot.repos.place

import java.time.LocalDateTime

import com.bot.DI._
import com.bot.models.repo.place.{Link, Logo, Place}
import com.bot.repos.SlickSupport
import com.bot.utils.DateTimeUtils
import com.bot.utils.PostgresProfiler.api._
import slick.lifted.ProvenShape

import scala.concurrent.Future

trait ToiletRepo {

  def findNextById(id: Long): Future[Option[Place]]

  def findByCityId(cityId: Long): Future[Seq[Place]]

  def findByGuildId(childGuildId: Long): Future[Seq[Place]]

  def findByUUID(uuid: String): Future[Option[Place]]

  def get: Future[Seq[Place]]

  def save(rate: Place): Future[Option[Long]]

  def update(rate: Place): Future[Option[Long]]

  def delete(id: Long): Future[Boolean]

  def isDeleted(id: Long): Future[Boolean]

}

object ToiletRepoImpl extends ToiletRepo with ToiletTableComponent {

  override def get: Future[Seq[Place]] = {
    val query = toiletTable
    db.run(query.result)
  }

  override def findNextById(id: Long): Future[Option[Place]] = {

    val query = toiletTable
      .filter(table => table.id > id)
      .result
      .headOption
    db.run(query).flatMap { nCity =>
      if (nCity.isDefined) Future.successful(nCity) else {
        val q = toiletTable
          .sortBy(_.id)
          .result
          .headOption
        db.run(q)
      }
    }

  }

  override def findByUUID(uuid: String): Future[Option[Place]] = {

    val query = toiletTable
      .filter(_.uuid === uuid)
      .result
      .headOption
    db.run(query)

  }

  override def findByGuildId(childGuildId: Long): Future[Seq[Place]] = {

    ??? // todo: need to implement
    //    val query = rateTable
    //      .filter(_.childGuildIds inSet childGuildIds)
    //      .result
    //    db.run(query)

  }

  override def findByCityId(cityId: Long): Future[Seq[Place]] = {

    val query = toiletTable
      .filter(_.cityId === cityId)
      .result
    db.run(query)

  }

  override def save(place: Place): Future[Option[Long]] = {

    val action = toiletTable returning toiletTable.map(_.id) into ((table, id) =>
      table.copy(id = Some(id))) += place.copy(createdAt = LocalDateTime.now)
    db.run(action).map(_.id)

  }

  override def update(place: Place): Future[Option[Long]] = {

    val action = toiletTable
      .filter(_.id === place.id)
      .update(place.copy(modifiedAt = DateTimeUtils.nowOpt))
    db.run(action).map(_ => place.id)

  }

  override def delete(id: Long): Future[Boolean] = {

    val action = toiletTable
      .filter(_.id === id)
      .map(x => (x.modifiedAt, x.deleted))
      .update((DateTimeUtils.nowOpt, true))

    db.run(action).map(_ > 0)

  }

  override def isDeleted(id: Long): Future[Boolean] = {

    val query = toiletTable
      .filter(_.id === id)
      .map(_.deleted)
    db.run(query.result.head)

  }

}

private[repos] trait ToiletTableComponent extends SlickSupport {

  private[ToiletTableComponent] final class ToiletTable(tag: Tag)
    extends Table[Place](tag, "toilet") {

    def id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def cityId: Rep[Long] = column[Long]("city_id")

    def childGuildIds: Rep[List[Long]] = column[List[Long]]("child_guild_ids")

    def uuid: Rep[Option[String]] = column[Option[String]]("uuid")

    def title: Rep[Option[String]] = column[Option[String]]("title")

    def address: Rep[Option[String]] = column[Option[String]]("address")

    def phone: Rep[Option[String]] = column[Option[String]]("phone")

    def lat: Rep[Option[Double]] = column[Option[Double]]("lat")

    def lng: Rep[Option[Double]] = column[Option[Double]]("lng")

    def logo: Rep[Option[Logo]] = column[Option[Logo]]("logo")

    def commentsCount: Rep[Option[Int]] = column[Option[Int]]("comments_count")

    def hasActiveContract: Rep[Option[Boolean]] = column[Option[Boolean]]("has_active_contract")

    def hygieneStatus: Rep[Option[String]] = column[Option[String]]("hygiene_status")

    def links: Rep[Option[Link]] = column[Option[Link]]("links")

    def provider: Rep[Option[String]] = column[Option[String]]("provider")

    def checkInCount: Rep[Option[Int]] = column[Option[Int]]("check_in_count")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")

    def modifiedAt: Rep[Option[LocalDateTime]] = column[Option[LocalDateTime]]("modified_at")

    def deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))

    def * : ProvenShape[Place] = (
      id.?,
      cityId,
      childGuildIds,
      uuid,
      title,
      address,
      phone,
      lat,
      lng,
      logo,
      commentsCount,
      hasActiveContract,
      hygieneStatus,
      links,
      provider,
      checkInCount,
      createdAt,
      modifiedAt,
      deleted) <> ((Place.apply _).tupled, Place.unapply)
  }

  protected val toiletTable = TableQuery[ToiletTable]

}