package com.bot.repos.review

import java.time.LocalDateTime

import com.bot.DI._
import com.bot.models.repo.review.{QuestionSubGuild, UserLike}
import com.bot.repos.SlickSupport
import com.bot.utils.DateTimeUtils
import com.bot.utils.PostgresProfiler.api._
import slick.lifted.ProvenShape

import scala.concurrent.Future

trait QuestionSubGuildRepo {

  def find(subGuildId: Long, questionId: Long): Future[Option[QuestionSubGuild]]

  def get(): Future[Seq[QuestionSubGuild]]

  def get(subGuildId: Long, offset: Int, length: Int): Future[Seq[QuestionSubGuild]]

  def save(questionSubGuild: QuestionSubGuild): Future[Option[Long]]

  def update(questionSubGuild: QuestionSubGuild): Future[Option[Long]]

  def delete(id: Long): Future[Boolean]

  def isDeleted(id: Long): Future[Boolean]

}

object QuestionSubGuildRepoImpl extends QuestionSubGuildRepo with QuestionSubGuildTableComponent {

  override def get(): Future[Seq[QuestionSubGuild]] = {
    val query = questionSubGuildTable
    db.run(query.result)
  }

  override def get(subGuildId: Long, offset: Int, length: Int): Future[Seq[QuestionSubGuild]] = {
    val query = questionSubGuildTable
        .filter(_.subGuildId === subGuildId)
        .drop(offset)
        .take(length)
    db.run(query.result)
  }

  override def find(subGuildId: Long, questionId: Long): Future[Option[QuestionSubGuild]] = {

    val query = questionSubGuildTable
      .filter(_.subGuildId === subGuildId)
      .filter(_.questionId === questionId)
      .result
      .headOption
    db.run(query)

  }

  override def save(questionSubGuild: QuestionSubGuild): Future[Option[Long]] = {

    val action = questionSubGuildTable returning questionSubGuildTable.map(_.id) into ((table, id) =>
      table.copy(id = Some(id))) += questionSubGuild.copy(createdAt = LocalDateTime.now)
    db.run(action).map(_.id)

  }

  override def update(questionSubGuild: QuestionSubGuild): Future[Option[Long]] = {

    val action = questionSubGuildTable
      .filter(_.id === questionSubGuild.id)
      .update(questionSubGuild.copy(modifiedAt = DateTimeUtils.nowOpt))
    db.run(action).map(x => questionSubGuild.id)

  }

  override def delete(id: Long): Future[Boolean] = {

    val action = questionSubGuildTable
      .filter(_.id === id)
      .map(x => (x.modifiedAt, x.deleted))
      .update((DateTimeUtils.nowOpt, true))

    db.run(action).map(_ > 0)

  }

  override def isDeleted(id: Long): Future[Boolean] = {

    val query = questionSubGuildTable
      .filter(_.id === id)
      .map(_.deleted)
    db.run(query.result.head)

  }

}

private[repos] trait QuestionSubGuildTableComponent extends SlickSupport {

  private[QuestionSubGuildTableComponent] final class QuestionSubGuildTable(tag: Tag)
    extends Table[QuestionSubGuild](tag, "question_sub_guild") {

    def id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def subGuildId: Rep[Long] = column[Long]("sub_guild_id")

    def questionId: Rep[Long] = column[Long]("question_id")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")

    def modifiedAt: Rep[Option[LocalDateTime]] = column[Option[LocalDateTime]]("modified_at")

    def deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))

    def * : ProvenShape[QuestionSubGuild] = (
      id.?,
      subGuildId,
      questionId,
      createdAt,
      modifiedAt,
      deleted) <> ((QuestionSubGuild.apply _).tupled, QuestionSubGuild.unapply)
  }

  protected val questionSubGuildTable = TableQuery[QuestionSubGuildTable]

}