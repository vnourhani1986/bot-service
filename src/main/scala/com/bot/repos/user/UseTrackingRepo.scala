package com.bot.repos.user

import java.time.LocalDateTime

import com.bot.DI._
import com.bot.models.repo.user.UserTracking
import com.bot.repos.SlickSupport
import com.bot.utils.DateTimeUtils
import com.bot.utils.PostgresProfiler.api._
import slick.lifted.ProvenShape

import scala.concurrent.Future

trait UserTrackingRepo {

  def get(userId: Long): Future[Option[UserTracking]]

  def get: Future[Seq[UserTracking]]

  def save(userTracking: UserTracking): Future[UserTracking]

  def update(userTracking: UserTracking): Future[Boolean]

  def isDisabled(userId: Long): Future[Boolean]

}

object UserTrackingRepoImpl extends UserTrackingRepo with UserTrackingTableComponent {

  override def get: Future[Seq[UserTracking]] = {
    val query = userTrackingTable
    db.run(query.result)
  }

  override def get(userId: Long): Future[Option[UserTracking]] = {
    val query = userTrackingTable
      .filter(_.userId === userId)
      .sortBy(_.createdAt.desc)
      .result.headOption
    db.run(query)
  }

  override def save(userTracking: UserTracking): Future[UserTracking] = {

    val action = userTrackingTable returning userTrackingTable.map(_.id) into ((table, id) =>
      table.copy(id = Some(id))) += userTracking.copy(createdAt = DateTimeUtils.now)
    db.run(action)

  }

  override def update(userTracking: UserTracking): Future[Boolean] = {

    val action = userTrackingTable
      .filter(_.id === userTracking.id)
      .update(userTracking.copy(modifiedAt = DateTimeUtils.nowOpt))

    db.run(action).map(_ > 0)

  }

  override def isDisabled(userId: Long): Future[Boolean] = {

    val query = userTrackingTable
      .filter(_.userId === userId)
      .map(_.disabled)

    db.run(query.result.head)

  }

}

private[repos] trait UserTrackingTableComponent extends SlickSupport {

  private[UserTrackingTableComponent] final class UserTrackingTable(tag: Tag)
    extends Table[UserTracking](tag, "user_tracking") {

    def id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def userId: Rep[Long] = column[Long]("user_id")

    def lat: Rep[Double] = column[Double]("lat")

    def lng: Rep[Double] = column[Double]("lng")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")

    def modifiedAt: Rep[Option[LocalDateTime]] = column[Option[LocalDateTime]]("modified_at")

    def disabled: Rep[Boolean] = column[Boolean]("disabled", O.Default(false))

    def deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))

    def * : ProvenShape[UserTracking] = (
      id.?,
      userId,
      lat,
      lng,
      createdAt,
      modifiedAt,
      disabled,
      deleted) <> ((UserTracking.apply _).tupled, UserTracking.unapply)
  }

  protected val userTrackingTable = TableQuery[UserTrackingTable]

}