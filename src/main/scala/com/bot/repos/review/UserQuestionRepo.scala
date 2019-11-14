package com.bot.repos.review

import java.time.LocalDateTime

import com.bot.DI._
import com.bot.models.repo.review.UserQuestion
import com.bot.repos.SlickSupport
import com.bot.utils.DateTimeUtils
import com.bot.utils.PostgresProfiler.api._
import slick.lifted.ProvenShape

import scala.concurrent.Future

trait UserQuestionRepo {

  def findById(id: Long): Future[Option[UserQuestion]]

  def get: Future[Seq[UserQuestion]]

  def get(take: Int): Future[Seq[UserQuestion]]

  def getByOffset(offset: Int): Future[Option[UserQuestion]]

  def getLength: Future[Int]

  def save(userQuestion: UserQuestion): Future[Option[Long]]

  def update(userQuestion: UserQuestion): Future[Option[Long]]

  def delete(id: Long): Future[Boolean]

  def isDeleted(id: Long): Future[Boolean]

}

object UserQuestionRepoImpl extends UserQuestionRepo with UserQuestionTableComponent {

  override def get: Future[Seq[UserQuestion]] = {
    val query = userQuestionTable
    db.run(query.result)
  }

  override def get(take: Int): Future[Seq[UserQuestion]] = {
    val query = userQuestionTable
        .take(take)
    db.run(query.result)
  }

  override def getByOffset(offset: Int): Future[Option[UserQuestion]] = {
    val query = userQuestionTable
        .drop(offset)
    db.run(query.result.headOption)
  }

  override def getLength: Future[Int] = {
    val query = userQuestionTable
        .length
    db.run(query.result)
  }

  override def findById(id: Long): Future[Option[UserQuestion]] = {

    val query = userQuestionTable
      .filter(_.id === id)
      .result
      .headOption
    db.run(query)

  }

  override def save(userQuestion: UserQuestion): Future[Option[Long]] = {

    val action = userQuestionTable returning userQuestionTable.map(_.id) into ((table, id) =>
      table.copy(id = Some(id))) += userQuestion.copy(createdAt = LocalDateTime.now)
    db.run(action).map(_.id)

  }

  override def update(userQuestion: UserQuestion): Future[Option[Long]] = {

    val action = userQuestionTable
      .filter(_.id === userQuestion.id)
      .update(userQuestion.copy(modifiedAt = DateTimeUtils.nowOpt))
    db.run(action).map(_ => userQuestion.id)

  }

  override def delete(id: Long): Future[Boolean] = {

    val action = userQuestionTable
      .filter(_.id === id)
      .map(x => (x.modifiedAt, x.deleted))
      .update((DateTimeUtils.nowOpt, true))

    db.run(action).map(_ > 0)

  }

  override def isDeleted(id: Long): Future[Boolean] = {

    val query = userQuestionTable
      .filter(_.id === id)
      .map(_.deleted)
    db.run(query.result.head)

  }

}

private[repos] trait UserQuestionTableComponent extends SlickSupport {

  private[UserQuestionTableComponent] final class UserQuestionTable(tag: Tag)
    extends Table[UserQuestion](tag, "user_question") {

    def id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def questionId: Rep[Long] = column[Long]("question_id")

    def title: Rep[String] = column[String]("title")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")

    def modifiedAt: Rep[Option[LocalDateTime]] = column[Option[LocalDateTime]]("modified_at")

    def provider: Rep[Option[String]] = column[Option[String]]("provider")

    def disabled: Rep[Boolean] = column[Boolean]("disabled", O.Default(false))

    def deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))

    def * : ProvenShape[UserQuestion] = (
      id.?,
      questionId,
      title,
      createdAt,
      modifiedAt,
      provider,
      disabled,
      deleted) <> ((UserQuestion.apply _).tupled, UserQuestion.unapply)
  }

  protected val userQuestionTable = TableQuery[UserQuestionTable]

}