package com.bot.repos.review

import java.time.LocalDateTime

import com.bot.DI._
import com.bot.models.repo.review.UserLike
import com.bot.repos.SlickSupport
import com.bot.utils.DateTimeUtils
import com.bot.utils.PostgresProfiler.api._
import slick.lifted.ProvenShape

import scala.concurrent.Future

trait UserLikeRepo {

  def find(placeId: Long): Future[Option[UserLike]]

  def find(userId: Long, reviewId: Long): Future[Option[UserLike]]

  def findById(id: Long): Future[Seq[UserLike]]

  def get: Future[Seq[UserLike]]

  def save(userLike: UserLike): Future[Option[Long]]

  def update(userLike: UserLike): Future[Option[Long]]

  def delete(id: Long): Future[Boolean]

  def isDeleted(id: Long): Future[Boolean]

}

object UserLikeRepoImpl extends UserLikeRepo with UserLikeTableComponent {

  override def get: Future[Seq[UserLike]] = {
    val query = userLikeTable
    db.run(query.result)
  }

  override def find(reviewId: Long): Future[Option[UserLike]] = {

    val query = userLikeTable
      .filter(_.reviewId === reviewId)
      .result
      .headOption
    db.run(query)

  }

  override def findById(id: Long): Future[Seq[UserLike]] = {

    val query = userLikeTable
      .filter(_.id === id)
      .result
    db.run(query)

  }

  override def find(userId: Long, reviewId: Long): Future[Option[UserLike]] = {

    val query = userLikeTable
      .filter(_.userId === userId)
      .filter(_.reviewId === reviewId)
      .result
      .headOption
    db.run(query)

  }

  override def save(userLike: UserLike): Future[Option[Long]] = {

    val action = userLikeTable returning userLikeTable.map(_.id) into ((table, id) =>
      table.copy(id = Some(id))) += userLike.copy(createdAt = LocalDateTime.now)
    db.run(action).map(_.id)

  }

  override def update(userLike: UserLike): Future[Option[Long]] = {

    val action = userLikeTable
      .filter(_.id === userLike.id)
      .update(userLike.copy(modifiedAt = DateTimeUtils.nowOpt))
    db.run(action).map(x => userLike.id)

  }

  override def delete(id: Long): Future[Boolean] = {

    val action = userLikeTable
      .filter(_.id === id)
      .map(x => (x.modifiedAt, x.deleted))
      .update((DateTimeUtils.nowOpt, true))

    db.run(action).map(_ > 0)

  }

  override def isDeleted(id: Long): Future[Boolean] = {

    val query = userLikeTable
      .filter(_.id === id)
      .map(_.deleted)
    db.run(query.result.head)

  }

}

private[repos] trait UserLikeTableComponent extends SlickSupport {

  private[UserLikeTableComponent] final class UserLikeTable(tag: Tag)
    extends Table[UserLike](tag, "user_review_like") {

    def id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def userId: Rep[Long] = column[Long]("user_id")

    def reviewId: Rep[Long] = column[Long]("review_id")

    def like: Rep[Boolean] = column[Boolean]("like")

    def provider: Rep[Option[String]] = column[Option[String]]("provider")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")

    def modifiedAt: Rep[Option[LocalDateTime]] = column[Option[LocalDateTime]]("modified_at")

    def deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))

    def * : ProvenShape[UserLike] = (
      id.?,
      userId,
      reviewId,
      like,
      provider,
      createdAt,
      modifiedAt,
      deleted) <> ((UserLike.apply _).tupled, UserLike.unapply)
  }

  protected val userLikeTable = TableQuery[UserLikeTable]

}