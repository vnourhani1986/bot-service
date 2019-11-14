package com.bot.repos.filter

import java.time.LocalDateTime

import com.bot.DI._
import com.bot.models.repo.filter.Filter
import com.bot.repos.SlickSupport
import com.bot.utils.DateTimeUtils
import com.bot.utils.PostgresProfiler.api._
import slick.lifted.ProvenShape
import spray.json.JsValue

import scala.concurrent.Future

trait FilterRepo {

  def get: Future[Seq[Filter]]

  def save(filter: Filter): Future[Filter]

  def update(filter: Filter): Future[Boolean]

  def isDisabled(userId: Long): Future[Boolean]

}

object FilterRepoImpl extends FilterRepo with FilterTableComponent {

  override def get: Future[Seq[Filter]] = {
    val query = filterTable
    db.run(query.result)
  }

  override def save(filter: Filter): Future[Filter] = {

    val action = filterTable returning filterTable.map(_.id) into ((table, id) =>
      table.copy(id = Some(id))) += filter.copy(createdAt = DateTimeUtils.now)
    db.run(action)

  }

  override def update(filter: Filter): Future[Boolean] = {

    val action = filterTable
      .filter(_.id === filter.id)
      .update(filter.copy(modifiedAt = DateTimeUtils.nowOpt))

    db.run(action).map(_ > 0)

  }

  override def isDisabled(id: Long): Future[Boolean] = {

    val query = filterTable
      .filter(_.id === id)
      .map(_.disabled)

    db.run(query.result.head)

  }

}

private[repos] trait FilterTableComponent extends SlickSupport {

  private[FilterTableComponent] final class FilterTable(tag: Tag)
    extends Table[Filter](tag, "filter") {

    def id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    def title: Rep[String] = column[String]("title")

    def data: Rep[String] = column[String]("data")

    def metaData: Rep[Option[JsValue]] = column[Option[JsValue]]("meta_data")

    def createdAt: Rep[LocalDateTime] = column[LocalDateTime]("created_at")

    def modifiedAt: Rep[Option[LocalDateTime]] = column[Option[LocalDateTime]]("modified_at")

    def disabled: Rep[Boolean] = column[Boolean]("disabled", O.Default(false))

    def deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))

    def * : ProvenShape[Filter] = (
      id.?,
      title,
      data,
      metaData,
      createdAt,
      modifiedAt,
      disabled,
      deleted) <> ((Filter.apply _).tupled, Filter.unapply)
  }

  protected val filterTable = TableQuery[FilterTable]

}