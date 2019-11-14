package com.bot.repos.user

import java.time.{LocalDate, LocalDateTime}

import com.bot.DI._
import com.bot.models.repo.user.User
import com.bot.repos.SlickSupport
import com.bot.utils.DateTimeUtils
import com.bot.utils.PostgresProfiler.api._
import slick.lifted.ProvenShape
import spray.json.JsValue

import scala.concurrent.Future

trait UserRepo {

  def findByUserName(userName: String): Future[Option[User]]

  def findOptionalByUserName(userName: Option[String]): Future[Option[User]]

  def findById(id: Long): Future[Option[User]]

  def get: Future[Seq[User]]

  def save(user: User): Future[User]

  def update(user: User): Future[Boolean]

  def insertOrUpdate(user: User): Future[Int]

  def isDisabled(userName: String): Future[Boolean]

}

object UserRepoImpl extends UserRepo with UserTableComponent {

  override def findByUserName(userName: String): Future[Option[User]] = {
    val query = userTable.filter(_.userName === userName).result.headOption
    db.run(query)
  }

  override def findOptionalByUserName(userName: Option[String]): Future[Option[User]] = {
    val query = userTable.filterOpt(userName)((table, un) => table.userName === un).result.headOption
    db.run(query)
  }

  override def findById(id: Long): Future[Option[User]] = {
    val query = userTable
      .filter(_.id === id)
      .result.headOption
    db.run(query)
  }

  override def get: Future[Seq[User]] = {
    val query = userTable
    db.run(query.result)
  }

  override def save(user: User): Future[User] = {

    val action = userTable returning userTable.map(_.id) into ((table, id) =>
      table.copy(id = Some(id))) += user.copy(createdAt = DateTimeUtils.now)
    db.run(action)

  }

  override def update(user: User): Future[Boolean] = {

    val action = userTable
      .filter(_.id === user.id)
      .update(user.copy(modifiedAt = DateTimeUtils.nowOpt))

    db.run(action).map(_ > 0)

  }

  override def insertOrUpdate(user: User): Future[Int] = {

    val action = userTable
//      .filter(_.userId === user.userId)
      .insertOrUpdate(user)
    db.run(action)

  }

  override def isDisabled(userName: String): Future[Boolean] = {

    val query = userTable
      .filter(_.userName === userName)
      .map(_.disabled)

    db.run(query.result.head)

  }

}

private[repos] trait UserTableComponent extends SlickSupport {

  private[UserTableComponent] final class UserTable(tag: Tag)
    extends Table[User](tag, "telegram_user") {

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey)

    def userName: Rep[Option[String]] = column[Option[String]]("user_name")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")

    def modifiedAt: Rep[Option[LocalDateTime]] = column[Option[LocalDateTime]]("modified_at")

    def firstName: Rep[Option[String]] = column[Option[String]]("first_name")

    def lastName: Rep[Option[String]] = column[Option[String]]("last_name")

    def isBot: Rep[Boolean] = column[Boolean]("is_bot")

    def email: Rep[Option[String]] = column[Option[String]]("email")

    def mobileNo: Rep[Option[String]] = column[Option[String]]("mobile_no")

    def langCode: Rep[Option[String]] = column[Option[String]]("language_code")

    def birthDate: Rep[Option[LocalDate]] = column[Option[LocalDate]]("birth_date")

    def gender: Rep[Option[String]] = column[Option[String]]("gender")

    def provider: Rep[Option[String]] = column[Option[String]]("provider")

    def score: Rep[Int] = column[Int]("score")

    def avatar: Rep[Option[JsValue]] = column[Option[JsValue]]("avatar")

    def totalReviews: Rep[Int] = column[Int]("total-reviews")

    def totalViews: Rep[Int] = column[Int]("total-views")

    def totalLikes: Rep[Int] = column[Int]("total-likes")

    def disabled: Rep[Boolean] = column[Boolean]("disabled", O.Default(false))

    def deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))

    def * : ProvenShape[User] = (
      id.?,
      userName,
      createdAt,
      modifiedAt,
      firstName,
      lastName,
      isBot,
      email,
      mobileNo,
      langCode,
      birthDate,
      gender,
      provider,
      score,
      avatar,
      totalReviews,
      totalViews,
      totalLikes,
      disabled,
      deleted) <> ((User.apply _).tupled, User.unapply)
  }

  protected val userTable = TableQuery[UserTable]

}