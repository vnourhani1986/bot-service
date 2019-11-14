package com.bot.repos.place

import java.time.LocalDateTime

import com.bot.DI._
import com.bot.models.repo.place.{Logo, Place, QueryPlace}
import com.bot.repos.SlickSupport
import com.bot.utils.DateTimeUtils
import com.bot.utils.PostgresProfiler.api._
import slick.lifted.ProvenShape

import scala.concurrent.Future

trait QueryPlaceRepo {

  def find(q: String): Future[Seq[QueryPlace]]

  def find(q: String, lat: Option[Double], lng: Option[Double], page: Int): Future[Seq[QueryPlace]]

  def findNextById(id: Long): Future[Option[QueryPlace]]

  def findByUUID(placeUUID: String): Future[Option[QueryPlace]]

  def findByPlaceId(placeId: Long): Future[Option[QueryPlace]]

  def findById(id: Long): Future[Option[QueryPlace]]

  def get: Future[Seq[QueryPlace]]

  def get(offset: Long, len: Long): Future[Seq[QueryPlace]]

  def save(rate: QueryPlace): Future[Option[Long]]

  def update(rate: QueryPlace): Future[Option[Long]]

  def delete(id: Long): Future[Boolean]

  def isDeleted(id: Long): Future[Boolean]

}

object QueryPlaceRepoImpl extends QueryPlaceRepo with QueryPlaceTableComponent {

  override def get: Future[Seq[QueryPlace]] = {
    val query = queryPlaceTable
      .sortBy(_.id)
    db.run(query.result)
  }

  override def get(offset: Long, len: Long): Future[Seq[QueryPlace]] = {
    val query = queryPlaceTable
      .sortBy(_.id)
      .filter(_.id > offset)
      .filter(_.id < offset + len)
    db.run(query.result)
  }

  override def find(q: String): Future[Seq[QueryPlace]] = {

    val qs = q.split(" ").filter(_ != "").foldLeft("")((nq, qq) => s"""$qq:* & $nq""")
    val qsf = qs.substring(0, if (q.length == 0) 0 else qs.length - 2)
    val query = sql"""SELECT id, place_id, sub_guilds, title, address, lat, lng FROM query_place WHERE to_tsvector('simple', coalesce(sub_guilds) || ' ' || coalesce(title) || ' ' || coalesce(address)) @@ to_tsquery('simple', $qsf) and deleted = false LIMIT 50;"""
      .as[(Option[Long], Long, Option[String], Option[String], Option[String], Option[Double], Option[Double])]
    db.run(query).map(res => {
      res.map{r =>
        val (id, placeId, sGuilds, title, address, lat, lng) = r
        QueryPlace(
          id = id,
          placeId = placeId,
          subGuilds = sGuilds,
          title = title,
          address = address,
          lat = lat,
          lng = lng,
          createdAt = LocalDateTime.now
        )
      }
    })
  }

  override def find(q: String, lat: Option[Double], lng: Option[Double], page: Int): Future[Seq[QueryPlace]] = {

    val qs = q.split(" ").filter(_ != "").foldLeft("")((nq, qq) => s"""$qq:* & $nq""")
    val qsf = qs.substring(0, if (q.length == 0) 0 else qs.length - 2)
    val offset = page * 10
    val query = sql"""SELECT id, place_id, sub_guilds, title, address, lat, lng, phone FROM query_place WHERE to_tsvector('simple', coalesce(sub_guilds) || ' ' || coalesce(title) || ' ' || coalesce(address)) @@ to_tsquery('simple', $qsf) and deleted = false OFFSET $offset LIMIT 10;"""
      .as[(Option[Long], Long, Option[String], Option[String], Option[String], Option[Double], Option[Double], Option[String])]
    db.run(query).map(res => {
      res.map{r =>
        val (id, placeId, sGuilds, title, address, lat, lng, phone) = r
        QueryPlace(
          id = id,
          placeId = placeId,
          subGuilds = sGuilds,
          title = title,
          address = address,
          phone = phone,
          lat = lat,
          lng = lng,
          createdAt = LocalDateTime.now
        )
      }
    })
  }

  override def findNextById(id: Long): Future[Option[QueryPlace]] = {

    val query = queryPlaceTable
      .filter(table => table.id > id)
      .result
      .headOption
    db.run(query).flatMap { nCity =>
      if (nCity.isDefined) Future.successful(nCity) else {
        val q = queryPlaceTable
          .sortBy(_.id)
          .result
          .headOption
        db.run(q)
      }
    }

  }

  override def findByPlaceId(placeId: Long): Future[Option[QueryPlace]] = {

    val query = queryPlaceTable
      .filter(_.placeId === placeId)
      .result
      .headOption
    db.run(query)

  }


  override def findByUUID(placeUUID: String): Future[Option[QueryPlace]] = {

    val query = queryPlaceTable
      .filter(_.placeUUID === placeUUID)
      .result
      .headOption
    db.run(query)

  }

  override def findById(id: Long): Future[Option[QueryPlace]] = {

    val query = queryPlaceTable
      .filter(_.id === id)
      .result
      .headOption
    db.run(query)

  }

  override def save(queryPlace: QueryPlace): Future[Option[Long]] = {

    val action = queryPlaceTable returning queryPlaceTable.map(_.id) into ((table, id) =>
      table.copy(id = Some(id))) += queryPlace.copy(createdAt = LocalDateTime.now)
    db.run(action).map(_.id)

  }

  override def update(queryPlace: QueryPlace): Future[Option[Long]] = {

    val action = queryPlaceTable
      .filter(_.id === queryPlace.id)
      .update(queryPlace.copy(modifiedAt = DateTimeUtils.nowOpt))
    db.run(action).map(x => queryPlace.id)

  }

  override def delete(id: Long): Future[Boolean] = {

    val action = queryPlaceTable
      .filter(_.id === id)
      .map(x => (x.modifiedAt, x.deleted))
      .update((DateTimeUtils.nowOpt, true))

    db.run(action).map(_ > 0)

  }

  override def isDeleted(id: Long): Future[Boolean] = {

    val query = queryPlaceTable
      .filter(_.id === id)
      .map(_.deleted)
    db.run(query.result.head)

  }

}

private[repos] trait QueryPlaceTableComponent extends SlickSupport {

  private[QueryPlaceTableComponent] final class QueryPlaceTable(tag: Tag)
    extends Table[QueryPlace](tag, "query_place") {

    def id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def placeId: Rep[Long] = column[Long]("place_id")

    def placeUUID: Rep[Option[String]] = column[Option[String]]("place_uuid")

    def subGuilds: Rep[Option[String]] = column[Option[String]]("sub_guilds")

    def title: Rep[Option[String]] = column[Option[String]]("title")

    def address: Rep[Option[String]] = column[Option[String]]("address")

    def phone: Rep[Option[String]] = column[Option[String]]("phone")

    def lat: Rep[Option[Double]] = column[Option[Double]]("lat")

    def lng: Rep[Option[Double]] = column[Option[Double]]("lng")

    def logo: Rep[Option[Logo]] = column[Option[Logo]]("logo")

    def provider: Rep[Option[String]] = column[Option[String]]("provider")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")

    def modifiedAt: Rep[Option[LocalDateTime]] = column[Option[LocalDateTime]]("modified_at")

    def deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))

    def * : ProvenShape[QueryPlace] = (
      id.?,
      placeId,
      placeUUID,
      subGuilds,
      title,
      address,
      phone,
      lat,
      lng,
      logo,
      provider,
      createdAt,
      modifiedAt,
      deleted) <> ((QueryPlace.apply _).tupled, QueryPlace.unapply)
  }

  protected val queryPlaceTable = TableQuery[QueryPlaceTable]

}
