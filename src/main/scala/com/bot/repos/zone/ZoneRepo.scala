package com.bot.repos.zone

import java.lang.Math._
import java.time.LocalDateTime

import com.bot.DI._
import com.bot.models.repo.zone.Zone
import com.bot.repos.SlickSupport
import com.bot.utils.{DateTimeUtils, ZoneUtil}
import com.bot.utils.PostgresProfiler.api._
import slick.lifted.ProvenShape

import scala.concurrent.Future

trait ZoneRepo {

  def get: Future[Seq[Zone]]

  def get(coordinate: Long, index: Int): Future[Seq[Long]]

  def get(lat: Double, lng: Double, radio: Double, offset: Int, length: Int): Future[(List[Long], Int)]

  def find(coordinate: Long): Future[Option[Zone]]

  def insertOrUpdate(zone: Zone): Future[Boolean]

  def save(rate: Zone): Future[Option[Long]]

  def update(rate: Zone): Future[Option[Long]]

  def delete(id: Long): Future[Boolean]

  def isDeleted(id: Long): Future[Boolean]

}

object ZoneRepoImpl extends ZoneRepo with ZoneTableComponent {

  override def get: Future[Seq[Zone]] = {
    val query = zoneTable
      .filter(_.disabled === false)
      .filter(_.deleted === false)
      .result
    db.run(query)
  }

  override def get(lat: Double, lng: Double, radio: Double, offset: Int, length: Int): Future[(List[Long], Int)] = {

    val rLat = radio * ZoneUtil.ONE_K_EQ_LAT_DEG
    val rLng = radio * (ZoneUtil.ONE_K_EQ_LNG_DEG_DOWN + ZoneUtil.ONE_K_EQ_LNG_DEG_UP) / 2d
    val query = zoneTable
      .filter(table => table.lat > lat - rLat && table.lat < lat + rLat)
      .filter(table => table.lng > lng - rLng && table.lng < lng + rLng)
      .filter(_.disabled === false)
      .filter(_.deleted === false)
      .sortBy(l => (l.lat - rLat) * (l.lat - rLat) + (l.lng - rLng) * (l.lng - rLng))
      .map(_.placeIds)
      .result

    db.run(query).map{ result =>
      val r = result.toList.flatten
      (r.slice(offset, offset + length), r.length)
    }

  }

  override def get(coordinate: Long, index: Int): Future[Seq[Long]] = {

    val div = pow(10d, index).toLong
    val query = zoneTable
      .filter(_.coordinate / div === coordinate / div)
      .filter(_.disabled === false)
      .filter(_.deleted === false)
      .map(_.placeIds)
      .result

    db.run(query).map(_.flatten)

  }

  override def find(coordinate: Long): Future[Option[Zone]] = {
    val query = zoneTable
      .filter(_.coordinate === coordinate)
      .filter(_.disabled === false)
      .filter(_.deleted === false)
      .result
      .headOption
    db.run(query)
  }

  override def insertOrUpdate(zone: Zone): Future[Boolean] = {

    val action = zoneTable
      .filter(_.coordinate === zone.coordinate)
      .insertOrUpdate(zone)
    db.run(action).map(_ > 0)

  }

  override def save(zone: Zone): Future[Option[Long]] = {

    val action = zoneTable returning zoneTable.map(_.id) into ((table, id) =>
      table.copy(id = Some(id))) += zone.copy(createdAt = LocalDateTime.now)
    db.run(action).map(_.id)

  }

  override def update(zone: Zone): Future[Option[Long]] = {

    val action = zoneTable
      .filter(_.id === zone.id)
      .update(zone.copy(modifiedAt = DateTimeUtils.nowOpt))
    db.run(action).map(_ => zone.id)

  }

  override def delete(id: Long): Future[Boolean] = {

    val action = zoneTable
      .filter(_.id === id)
      .map(x => (x.modifiedAt, x.deleted))
      .update((DateTimeUtils.nowOpt, true))

    db.run(action).map(_ > 0)

  }

  override def isDeleted(id: Long): Future[Boolean] = {

    val query = zoneTable
      .filter(_.id === id)
      .map(_.deleted)
    db.run(query.result.head)

  }

}

private[repos] trait ZoneTableComponent extends SlickSupport {

  private[ZoneTableComponent] final class ZoneTable(tag: Tag)
    extends Table[Zone](tag, "zone") {

    def id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def coordinate: Rep[Long] = column[Long]("coordinate")

    def lat: Rep[Double] = column[Double]("lat")

    def lng: Rep[Double] = column[Double]("lng")

    def placeIds: Rep[List[Long]] = column[List[Long]]("place_ids")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")

    def modifiedAt: Rep[Option[LocalDateTime]] = column[Option[LocalDateTime]]("modified_at")

    def disabled: Rep[Boolean] = column[Boolean]("disabled", O.Default(false))

    def deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))

    def * : ProvenShape[Zone] = (
      id.?,
      coordinate,
      lat,
      lng,
      placeIds,
      createdAt,
      modifiedAt,
      disabled,
      deleted) <> ((Zone.apply _).tupled, Zone.unapply)
  }

  protected val zoneTable = TableQuery[ZoneTable]

}