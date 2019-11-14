package com.bot.repos.place

import java.time.LocalDateTime

import com.bot.DI._
import com.bot.models.repo.place.{Link, Logo, Place}
import com.bot.repos.SlickSupport
import com.bot.utils.DateTimeUtils
import com.bot.utils.PostgresProfiler.api._
import slick.lifted.ProvenShape

import scala.concurrent.Future

trait PlaceRepo {

  def find(q: String): Future[Seq[Place]]

  def findNextById(id: Long): Future[Option[Place]]

  def findByCityId(cityId: Long): Future[Seq[Place]]

  def findByGuildId(childGuildId: Long): Future[Seq[Place]]

  def findByUUID(uuid: String): Future[Option[Place]]

  def findById(id: Long): Future[Option[Place]]

  def getByIds(ids: Seq[Long]): Future[Seq[Place]]

  def getByQueryIds(ids: Seq[Long], q: String): Future[Seq[Place]]

  def get: Future[Seq[Place]]

  def get(offset: Long, len: Long): Future[Seq[Place]]

  def getBySubGuildAndIds(ids: Seq[Long], subGuildIds: List[Long]): Future[Seq[Place]]

  def save(rate: Place): Future[Option[Long]]

  def update(rate: Place): Future[Option[Long]]

  def delete(id: Long): Future[Boolean]

  def isDeleted(id: Long): Future[Boolean]

}

object PlaceRepoImpl extends PlaceRepo with PlaceTableComponent {

  override def get: Future[Seq[Place]] = {
    val query = placeTable
      .sortBy(_.id)
    db.run(query.result)
  }

  override def get(offset: Long, len: Long): Future[Seq[Place]] = {
    val query = placeTable
      .sortBy(_.id)
      .filter(_.id > offset)
      .filter(_.id < offset + len)
    db.run(query.result)
  }

  override def getBySubGuildAndIds(ids: Seq[Long], subGuildIds: List[Long]): Future[Seq[Place]] = {
    val query = placeTable
      .filter(_.id inSet ids)
      .filter(_.childGuildIds @> subGuildIds)
      .result
    db.run(query)
  }

  override def getByIds(ids: Seq[Long]): Future[Seq[Place]] = {
    val query = placeTable
      .filter(_.id inSet ids)
      .sortBy(_.createdAt.desc)
      .take(50)
      .result
    db.run(query)
  }

  override def getByQueryIds(ids: Seq[Long], q: String): Future[Seq[Place]] = {
    val query = placeTable
      .filter(_.id inSet ids)
      .filter(_.title like s"""%$q%""")
      .sortBy(_.createdAt.desc)
      .take(50)
      .result
    db.run(query)
  }

  override def find(q: String): Future[Seq[Place]] = {

    val qs = q.split(" ").filter(_ != "").foldLeft("")((nq, qq) => s"""$qq:* & $nq""")
    val qsf = qs.substring(0, qs.length - 2)
    val query = sql"""SELECT id, city_id, title, address, lat, lng FROM place WHERE to_tsvector('simple', coalesce(title) || ' ' || coalesce(address)) @@ to_tsquery('simple', $qsf) LIMIT 50;"""
      .as[(Option[Long], Long, Option[String], Option[String], Option[Double], Option[Double])]
    db.run(query).map(res => {
      res.map { r =>
        val (id, cityId, title, address, lat, lng) = r
        Place(
          id = id,
          cityId = cityId,
          title = title,
          address = address,
          lat = lat,
          lng = lng,
          createdAt = LocalDateTime.now
        )
      }
    })
  }

  override def findNextById(id: Long): Future[Option[Place]] = {

    val query = placeTable
      .filter(table => table.id > id)
      .result
      .headOption
    db.run(query).flatMap { nCity =>
      if (nCity.isDefined) Future.successful(nCity) else {
        val q = placeTable
          .sortBy(_.id)
          .result
          .headOption
        db.run(q)
      }
    }

  }

  override def findByUUID(uuid: String): Future[Option[Place]] = {

    val query = placeTable
      .filter(_.uuid === uuid)
      .result
      .headOption
    db.run(query)

  }

  override def findById(id: Long): Future[Option[Place]] = {

    val query = placeTable
      .filter(_.id === id)
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

    val query = placeTable
      .filter(_.cityId === cityId)
      .result
    db.run(query)

  }

  override def save(place: Place): Future[Option[Long]] = {

    val action = placeTable returning placeTable.map(_.id) into ((table, id) =>
      table.copy(id = Some(id))) += place.copy(createdAt = LocalDateTime.now)
    db.run(action).map(_.id)

  }

  override def update(place: Place): Future[Option[Long]] = {

    val action = placeTable
      .filter(_.id === place.id)
      .update(place.copy(modifiedAt = DateTimeUtils.nowOpt))
    db.run(action).map(_ => place.id)

  }

  override def delete(id: Long): Future[Boolean] = {

    val action = placeTable
      .filter(_.id === id)
      .map(x => (x.modifiedAt, x.deleted))
      .update((DateTimeUtils.nowOpt, true))

    db.run(action).map(_ > 0)

  }

  override def isDeleted(id: Long): Future[Boolean] = {

    val query = placeTable
      .filter(_.id === id)
      .map(_.deleted)
    db.run(query.result.head)

  }

}

private[repos] trait PlaceTableComponent extends SlickSupport {

  private[PlaceTableComponent] final class PlaceTable(tag: Tag)
    extends Table[Place](tag, "place") {

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

  protected val placeTable = TableQuery[PlaceTable]

}


//object runner extends App {
////  PlaceRepoImpl.find("تهران آهنگری").map(println)
//  val q = s""" تهران ولعصر سی"""
//  val qs = q.split(" ").filter(_ != "").foldLeft("")((nq, qq) => s"""$qq:* & $nq""")
//  val qsf = qs.substring(0, qs.length - 2)
//  println(q.split(" ").filter(_ != "").toList)
//  println(qsf)
//}
//
