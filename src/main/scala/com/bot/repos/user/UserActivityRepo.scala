package com.bot.repos.user

import java.time.LocalDateTime

import com.bot.DI._
import com.bot.models.repo.user.UserActivity
import com.bot.repos.SlickSupport
import com.bot.utils.DateTimeUtils
import com.bot.utils.PostgresProfiler.api._
import slick.lifted.ProvenShape
import spray.json.JsValue

import scala.concurrent.Future

trait UserActivityRepo {

  def findByUserId(userId: Long): Future[Option[UserActivity]]

  def get: Future[Seq[UserActivity]]

  def getLast(userId: Long, noAction: String): Future[Option[UserActivity]]

  def getLast(userId: Long, action: Option[String] = None): Future[Option[UserActivity]]

  def save(userActivity: UserActivity): Future[UserActivity]

  def update(userActivity: UserActivity): Future[Boolean]

  def isDisabled(userId: Long): Future[Boolean]

}

object UserActivityRepoImpl extends UserActivityRepo with UserActivityTableComponent {

  override def findByUserId(userId: Long): Future[Option[UserActivity]] = {
    val query = userActivityTable
      .filter(_.userId === userId)
      .result.headOption
    db.run(query)
  }

  override def get: Future[Seq[UserActivity]] = {
    val query = userActivityTable
    db.run(query.result)
  }

  override def getLast(userId: Long, noAction: String): Future[Option[UserActivity]] = {
    val query = userActivityTable
      .filter(_.userId === userId)
      .filter(_.action =!= noAction)
      .sortBy(_.id.desc)
      .result
      .headOption
    db.run(query)
  }

  override def getLast(userId: Long, action: Option[String] = None): Future[Option[UserActivity]] = {
    val query = userActivityTable
      .filter(_.userId === userId)
      .filterOpt(action)((table, act) => table.action === act)
      .sortBy(_.id.desc)
      .result
      .headOption
    db.run(query)
  }


  override def save(userActivity: UserActivity): Future[UserActivity] = {

    val action = userActivityTable returning userActivityTable.map(_.id) into ((table, id) =>
      table.copy(id = Some(id))) += userActivity.copy(createdAt = DateTimeUtils.now)
    db.run(action)

  }

  override def update(userActivity: UserActivity): Future[Boolean] = {

    val action = userActivityTable
      .filter(_.id === userActivity.id)
      .update(userActivity.copy(modifiedAt = DateTimeUtils.nowOpt))

    db.run(action).map(_ > 0)

  }

  override def isDisabled(userId: Long): Future[Boolean] = {

    val query = userActivityTable
      .filter(_.userId === userId)
      .map(_.disabled)

    db.run(query.result.head)

  }

}

private[repos] trait UserActivityTableComponent extends SlickSupport {

  private[UserActivityTableComponent] final class UserActivityTable(tag: Tag)
    extends Table[UserActivity](tag, "user_activity") {

    def id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def userId: Rep[Long] = column[Long]("user_id")

    def chatId: Rep[Long] = column[Long]("chat_id")

    def entityId: Rep[Option[Long]] = column[Option[Long]]("entity_id")

    def action: Rep[String] = column[String]("action")

    def metaData: Rep[Option[JsValue]] = column[Option[JsValue]]("meta_data")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")

    def modifiedAt: Rep[Option[LocalDateTime]] = column[Option[LocalDateTime]]("modified_at")

    def disabled: Rep[Boolean] = column[Boolean]("disabled", O.Default(false))

    def deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))

    def * : ProvenShape[UserActivity] = (
      id.?,
      userId,
      chatId,
      entityId,
      action,
      metaData,
      createdAt,
      modifiedAt,
      disabled,
      deleted) <> ((UserActivity.apply _).tupled, UserActivity.unapply)
  }

  protected val userActivityTable = TableQuery[UserActivityTable]

}