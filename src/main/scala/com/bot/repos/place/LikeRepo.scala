package com.bot.repos.place

import java.time.LocalDateTime

import com.bot.DI._
import com.bot.models.repo.place.Like
import com.bot.repos.SlickSupport
import com.bot.utils.DateTimeUtils
import com.bot.utils.PostgresProfiler.api._
import slick.lifted.ProvenShape

import scala.concurrent.Future

trait LikeRepo {

  def find(placeId: Long): Future[Seq[Like]]

  def find(userId: Long, placeId: Long): Future[Option[Like]]

  def get: Future[Seq[Like]]

  def save(like: Like): Future[Option[Long]]

  def update(like: Like): Future[Option[Long]]

  def delete(id: Long): Future[Boolean]

  def isDeleted(id: Long): Future[Boolean]

}

object LikeRepoImpl extends LikeRepo with LikeTableComponent {

  override def get: Future[Seq[Like]] = {
    val query = likeTable
    db.run(query.result)
  }

  override def find(placeId: Long): Future[Seq[Like]] = {

    val query = likeTable
      .filter(_.placeId === placeId)
      .result
    db.run(query)

  }

  override def find(userId: Long, placeId: Long): Future[Option[Like]] = {

    val query = likeTable
      .filter(_.placeId === placeId)
      .result
      .headOption
    db.run(query)

  }

  override def save(like: Like): Future[Option[Long]] = {

    val action = likeTable returning likeTable.map(_.id) into ((table, id) =>
      table.copy(id = Some(id))) += like.copy(createdAt = LocalDateTime.now)
    db.run(action).map(_.id)

  }

  override def update(like: Like): Future[Option[Long]] = {

    val action = likeTable
      .filter(_.id === like.id)
      .update(like.copy(modifiedAt = DateTimeUtils.nowOpt))
    db.run(action).map(x => like.id)

  }

  override def delete(id: Long): Future[Boolean] = {

    val action = likeTable
      .filter(_.id === id)
      .map(x => (x.modifiedAt, x.deleted))
      .update((DateTimeUtils.nowOpt, true))

    db.run(action).map(_ > 0)

  }

  override def isDeleted(id: Long): Future[Boolean] = {

    val query = likeTable
      .filter(_.id === id)
      .map(_.deleted)
    db.run(query.result.head)

  }

}

private[repos] trait LikeTableComponent extends SlickSupport {

  private[LikeTableComponent] final class LikeTable(tag: Tag)
    extends Table[Like](tag, "place_like") {

    def id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def userId: Rep[Long] = column[Long]("user_id")

    def placeId: Rep[Long] = column[Long]("place_id")

    def like: Rep[Boolean] = column[Boolean]("like")

    def provider: Rep[Option[String]] = column[Option[String]]("provider")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")

    def modifiedAt: Rep[Option[LocalDateTime]] = column[Option[LocalDateTime]]("modified_at")

    def deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))

    def * : ProvenShape[Like] = (
      id.?,
      userId,
      placeId,
      like,
      provider,
      createdAt,
      modifiedAt,
      deleted) <> ((Like.apply _).tupled, Like.unapply)
  }

  protected val likeTable = TableQuery[LikeTable]

}