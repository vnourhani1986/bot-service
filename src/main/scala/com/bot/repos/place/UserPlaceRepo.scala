package com.bot.repos.place

import java.time.LocalDateTime

import com.bot.DI._
import com.bot.models.repo.place.UserPlace
import com.bot.repos.SlickSupport
import com.bot.utils.DateTimeUtils
import com.bot.utils.PostgresProfiler.api._
import slick.lifted.ProvenShape

import scala.concurrent.Future

trait UserPlaceRepo {

  def find(userId: Long): Future[Seq[UserPlace]]

  def find(userId: Long, placeId: Long): Future[Option[UserPlace]]

  def get: Future[Seq[UserPlace]]

  def save(userPlace: UserPlace): Future[Option[Long]]

  def update(userPlace: UserPlace): Future[Option[Long]]

  def delete(id: Long): Future[Boolean]

  def isDeleted(id: Long): Future[Boolean]

}

object UserPlaceRepoImpl extends UserPlaceRepo with UserPlaceTableComponent {

  override def get: Future[Seq[UserPlace]] = {
    val query = userPlaceTable
    db.run(query.result)
  }

  override def find(userId: Long): Future[Seq[UserPlace]] = {

    val query = userPlaceTable
      .filter(_.userId === userId)
      .result
    db.run(query)

  }

  override def find(userId: Long, placeId: Long): Future[Option[UserPlace]] = {

    val query = userPlaceTable
      .filter(_.placeId === placeId)
      .result
      .headOption
    db.run(query)

  }

  override def save(userPlace: UserPlace): Future[Option[Long]] = {

    val action = userPlaceTable returning userPlaceTable.map(_.id) into ((table, id) =>
      table.copy(id = Some(id))) += userPlace.copy(createdAt = LocalDateTime.now)
    db.run(action).map(_.id)

  }

  override def update(userPlace: UserPlace): Future[Option[Long]] = {

    val action = userPlaceTable
      .filter(_.id === userPlace.id)
      .update(userPlace.copy(modifiedAt = DateTimeUtils.nowOpt))
    db.run(action).map(x => userPlace.id)

  }

  override def delete(id: Long): Future[Boolean] = {

    val action = userPlaceTable
      .filter(_.id === id)
      .map(x => (x.modifiedAt, x.deleted))
      .update((DateTimeUtils.nowOpt, true))

    db.run(action).map(_ > 0)

  }

  override def isDeleted(id: Long): Future[Boolean] = {

    val query = userPlaceTable
      .filter(_.id === id)
      .map(_.deleted)
    db.run(query.result.head)

  }

}

private[repos] trait UserPlaceTableComponent extends SlickSupport {

  private[UserPlaceTableComponent] final class UserPlaceTable(tag: Tag)
    extends Table[UserPlace](tag, "user_place") {

    def id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def userId: Rep[Long] = column[Long]("user_id")

    def placeId: Rep[Long] = column[Long]("place_id")

    def provider: Rep[Option[String]] = column[Option[String]]("provider")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")

    def modifiedAt: Rep[Option[LocalDateTime]] = column[Option[LocalDateTime]]("modified_at")

    def deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))

    def * : ProvenShape[UserPlace] = (
      id.?,
      userId,
      placeId,
      provider,
      createdAt,
      modifiedAt,
      deleted) <> ((UserPlace.apply _).tupled, UserPlace.unapply)
  }

  protected val userPlaceTable = TableQuery[UserPlaceTable]

}