package com.bot.repos.place

import java.time.LocalDateTime

import com.bot.DI._
import com.bot.models.repo.place.Question
import com.bot.repos.SlickSupport
import com.bot.utils.DateTimeUtils
import com.bot.utils.PostgresProfiler.api._
import slick.lifted.ProvenShape

import scala.concurrent.Future

trait QuestionRepo {

  def find(rateId: Long): Future[Seq[Question]]

  def get: Future[Seq[Question]]

  def save(question: Question): Future[Question]

  def update(question: Question): Future[Boolean]

  def delete(id: Long): Future[Boolean]

  def isDeleted(id: Long): Future[Boolean]

}

object QuestionRepoImpl extends QuestionRepo with QuestionTableComponent {

  override def get: Future[Seq[Question]] = {
    val query = questionTable
    db.run(query.result)
  }

  override def find(rateId: Long): Future[Seq[Question]] = {

    val query = questionTable
      .filter(_.rateId === rateId)
      .result
    db.run(query)

  }

  override def save(question: Question): Future[Question] = {

    val action = questionTable returning questionTable.map(_.id) into ((table, id) =>
      table.copy(id = Some(id))) += question.copy(createdAt = LocalDateTime.now)
    db.run(action)

  }

  override def update(question: Question): Future[Boolean] = {

    val action = questionTable
      .filter(_.id === question.id)
      .update(question.copy(modifiedAt = DateTimeUtils.nowOpt))
    db.run(action).map(_ > 0)

  }

  override def delete(id: Long): Future[Boolean] = {

    val action = questionTable
      .filter(_.id === id)
      .map(x => (x.modifiedAt, x.deleted))
      .update((DateTimeUtils.nowOpt, true))

    db.run(action).map(_ > 0)

  }

  override def isDeleted(id: Long): Future[Boolean] = {

    val query = questionTable
      .filter(_.id === id)
      .map(_.deleted)
    db.run(query.result.head)

  }

}

private[repos] trait QuestionTableComponent extends SlickSupport {

  private[QuestionTableComponent] final class QuestionTable(tag: Tag)
    extends Table[Question](tag, "place_rate_question") {

    def id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def rateId: Rep[Option[Long]] = column[Option[Long]]("rate_id")

    def overall: Rep[Option[Int]] = column[Option[Int]]("overall")

    def text: Rep[Option[String]] = column[Option[String]]("text")

    def title: Rep[Option[String]] = column[Option[String]]("title")

    def provider: Rep[Option[String]] = column[Option[String]]("provider")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")

    def modifiedAt: Rep[Option[LocalDateTime]] = column[Option[LocalDateTime]]("modified_at")

    def deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))

    def * : ProvenShape[Question] = (
      id.?,
      rateId,
      overall,
      text,
      title,
      provider,
      createdAt,
      modifiedAt,
      deleted) <> ((Question.apply _).tupled, Question.unapply)
  }

  protected val questionTable = TableQuery[QuestionTable]

}