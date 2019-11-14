package com.bot.repos.review

import java.time.LocalDateTime

import com.bot.DI._
import com.bot.models.repo.review.UserRate
import com.bot.repos.SlickSupport
import com.bot.utils.DateTimeUtils
import com.bot.utils.PostgresProfiler.api._
import slick.lifted.ProvenShape

import scala.concurrent.Future

trait UserRateRepo {

  def findByUserId(id: Long): Future[Option[UserRate]]

  def find(userId: Long, placeUUID: String): Future[Option[UserRate]]

  def get: Future[Seq[UserRate]]

  def save(userRate: UserRate): Future[Option[Long]]

  def update(userRate: UserRate): Future[Option[Long]]

  def delete(id: Long): Future[Boolean]

  def isDeleted(id: Long): Future[Boolean]

}

object UserRateRepoImpl extends UserRateRepo with UserRateTableComponent {

  override def get: Future[Seq[UserRate]] = {
    val query = userRateTable
    db.run(query.result)
  }

  override def findByUserId(id: Long): Future[Option[UserRate]] = {

    val query = userRateTable
      .filter(_.userId === id)
      .result
      .headOption
    db.run(query)

  }

  override def find(userId: Long, placeUUID: String): Future[Option[UserRate]] = {

    val query = userRateTable
      .filter(_.userId === userId)
      .filter(_.placeUUID === placeUUID)
      .result
      .headOption
    db.run(query)

  }


  override def save(userRate: UserRate): Future[Option[Long]] = {

    val action = userRateTable returning userRateTable.map(_.id) into ((table, id) =>
      table.copy(id = Some(id))) += userRate.copy(createdAt = LocalDateTime.now)
    db.run(action).map(_.id)

  }

  override def update(userRate: UserRate): Future[Option[Long]] = {

    val action = userRateTable
      .filter(_.id === userRate.id)
      .update(userRate.copy(modifiedAt = DateTimeUtils.nowOpt))
    db.run(action).map(x => if (x > 0) userRate.id else None)

  }

  override def delete(id: Long): Future[Boolean] = {

    val action = userRateTable
      .filter(_.id === id)
      .map(x => (x.modifiedAt, x.deleted))
      .update((DateTimeUtils.nowOpt, true))

    db.run(action).map(_ > 0)

  }

  override def isDeleted(id: Long): Future[Boolean] = {

    val query = userRateTable
      .filter(_.id === id)
      .map(_.deleted)
    db.run(query.result.head)

  }

}

private[repos] trait UserRateTableComponent extends SlickSupport {

  private[UserRateTableComponent] final class UserRateTable(tag: Tag)
    extends Table[UserRate](tag, "user_rate") {

    def id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def userId: Rep[Long] = column[Long]("user_id")

    def placeUUID: Rep[String] = column[String]("place_uuid")

    def questionIds: Rep[List[Long]] = column[List[Long]]("question_ids")

    def questionValues: Rep[List[Int]] = column[List[Int]]("question_values")

    def average: Rep[Option[Double]] = column[Option[Double]]("average")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")

    def modifiedAt: Rep[Option[LocalDateTime]] = column[Option[LocalDateTime]]("modified_at")

    def provider: Rep[Option[String]] = column[Option[String]]("provider")

    def disabled: Rep[Boolean] = column[Boolean]("disabled", O.Default(false))

    def deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))

    def * : ProvenShape[UserRate] = (
      id.?,
      userId,
      placeUUID,
      questionIds,
      questionValues,
      average,
      createdAt,
      modifiedAt,
      provider,
      disabled,
      deleted) <> ((UserRate.apply _).tupled, UserRate.unapply)
  }

  protected val userRateTable = TableQuery[UserRateTable]

}