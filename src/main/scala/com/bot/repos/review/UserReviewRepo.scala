package com.bot.repos.review

import java.sql.Timestamp
import java.time.LocalDateTime

import com.bot.DI._
import com.bot.models.repo.review.UserReview
import com.bot.repos.SlickSupport
import com.bot.utils.DateTimeUtils
import com.bot.utils.PostgresProfiler.api._
import slick.lifted.ProvenShape
import spray.json.JsValue

import scala.concurrent.Future

trait UserReviewRepo {

  def findByReviewId(id: Long): Future[Option[UserReview]]

  def get: Future[Seq[UserReview]]

  def get(take: Int, placeUUID: String): Future[Option[UserReview]]

  def getLast(placeUUID: String): Future[Option[UserReview]]

  def getTotalReviews(placeUUID: String): Future[Int]

  def save(userReview: UserReview): Future[Option[Long]]

  def update(userReview: UserReview): Future[Option[Long]]

  def delete(id: Long): Future[Boolean]

  def isDeleted(id: Long): Future[Boolean]

}

object UserReviewRepoImpl extends UserReviewRepo with UserReviewTableComponent {

  override def get: Future[Seq[UserReview]] = {
    val query = userReviewTable
    db.run(query.result)
  }

  override def get(take: Int, placeUUID: String): Future[Option[UserReview]] = {
    val query = userReviewTable
      .filter(_.placeUUID === placeUUID)
      .sortBy(_.createdAt.desc)
      .drop(take - 1)
      .result
      .headOption
    db.run(query)
  }

  override def getLast(placeUUID: String): Future[Option[UserReview]] = {
    val query = userReviewTable
      .filter(_.placeUUID === placeUUID)
      .sortBy(_.createdAt)
      .result
      .headOption
    db.run(query)
  }

  override def getTotalReviews(placeUUID: String): Future[Int] = {
    val query = userReviewTable
      .filter(_.placeUUID === placeUUID)
      .sortBy(_.createdAt)
      .length
      .result
    db.run(query)
  }

  override def findByReviewId(id: Long): Future[Option[UserReview]] = {

    val query = userReviewTable
      .filter(_.reviewId === id)
      .result
      .headOption
    db.run(query)

  }

  override def save(userReview: UserReview): Future[Option[Long]] = {

    val action = userReviewTable returning userReviewTable.map(_.id) into ((table, id) =>
      table.copy(id = Some(id))) += userReview.copy(createdAt = LocalDateTime.now)
    db.run(action).map(_.id)

  }

  override def update(userReview: UserReview): Future[Option[Long]] = {

    val action = userReviewTable
      .filter(_.reviewId === userReview.reviewId)
      .update(userReview.copy(modifiedAt = DateTimeUtils.nowOpt))
    db.run(action).map(_ => userReview.id)

  }

  override def delete(id: Long): Future[Boolean] = {

    val action = userReviewTable
      .filter(_.id === id)
      .map(x => (x.modifiedAt, x.deleted))
      .update((DateTimeUtils.nowOpt, true))

    db.run(action).map(_ > 0)

  }

  override def isDeleted(id: Long): Future[Boolean] = {

    val query = userReviewTable
      .filter(_.id === id)
      .map(_.deleted)
    db.run(query.result.head)

  }

}

private[repos] trait UserReviewTableComponent extends SlickSupport {

  private[UserReviewTableComponent] final class UserReviewTable(tag: Tag)
    extends Table[UserReview](tag, "user_review") {

    def id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def reviewId: Rep[Long] = column[Long]("review_id")

    def userId: Rep[Long] = column[Long]("user_id")

    def placeUUID: Rep[String] = column[String]("place_uuid")

    def content: Rep[Option[String]] = column[Option[String]]("content")

    def images: Rep[JsValue] = column[JsValue]("images")

    def repliesCount: Rep[Option[Int]] = column[Option[Int]]("replies_count")

    def repliesItems: Rep[Option[JsValue]] = column[Option[JsValue]]("replies_items")

    def likesCount: Rep[Option[Int]] = column[Option[Int]]("likes_count")

    def likesItems: Rep[Option[JsValue]] = column[Option[JsValue]]("likes_items")

    def isLikeByUser: Rep[Option[Boolean]] = column[Option[Boolean]]("is_Like_by_user")

    def reviewCreatedAt: Rep[Option[Timestamp]] = column[Option[Timestamp]]("dunro_created_at")

    def links: Rep[Option[JsValue]] = column[Option[JsValue]]("links")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")

    def modifiedAt: Rep[Option[LocalDateTime]] = column[Option[LocalDateTime]]("modified_at")

    def provider: Rep[Option[String]] = column[Option[String]]("provider")

    def disabled: Rep[Boolean] = column[Boolean]("disabled", O.Default(false))

    def deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))

    def * : ProvenShape[UserReview] = (
      id.?,
      reviewId,
      userId,
      placeUUID,
      content,
      images,
      repliesCount,
      repliesItems,
      likesCount,
      likesItems,
      isLikeByUser,
      reviewCreatedAt,
      links,
      createdAt,
      modifiedAt,
      provider,
      disabled,
      deleted) <> ((UserReview.apply _).tupled, UserReview.unapply)
  }

  protected val userReviewTable = TableQuery[UserReviewTable]

}