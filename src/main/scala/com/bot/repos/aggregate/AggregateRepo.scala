package com.bot.repos.aggregate

import com.bot.DI._
import com.bot.models.repo.place.Place
import com.bot.models.repo.review.UserQuestion
import com.bot.repos.place.{PlaceTableComponent, QueryPlaceTableComponent, RateTableComponent}
import com.bot.repos.review.{QuestionSubGuildTableComponent, UserQuestionTableComponent}
import com.bot.repos.zone.ZoneTableComponent
import com.bot.utils.PostgresProfiler.api._
import com.bot.utils.ZoneUtil

import scala.concurrent.Future

trait AggregateRepo {

  def getPopPlaces(subGuildIds: List[Long], lat: Double, lng: Double, radio: Double): Future[(Seq[Place], Int)]

  def getNearPlaces(subGuildIds: List[Long], lat: Double, lng: Double, radio: Double, offset: Int, length: Int): Future[(Seq[Place], Int)]

  def getQuestions(subGuildId: Long, offset: Int, length: Int): Future[Seq[UserQuestion]]

}

object AggregateRepoImpl extends AggregateRepo
  with PlaceTableComponent
  with ZoneTableComponent
  with RateTableComponent
  with UserQuestionTableComponent
  with QuestionSubGuildTableComponent
  with QueryPlaceTableComponent {

  override def getPopPlaces(subGuildIds: List[Long], lat: Double, lng: Double, radio: Double): Future[(Seq[Place], Int)] = {

    val rLat = radio * ZoneUtil.ONE_K_EQ_LAT_DEG
    val rLng = radio * (ZoneUtil.ONE_K_EQ_LNG_DEG_DOWN + ZoneUtil.ONE_K_EQ_LNG_DEG_UP) / 2d
    val query = zoneTable
      .filter(!_.disabled)
      .filter(!_.deleted)
      .filter(table => table.lat > lat - rLat && table.lat < lat + rLat)
      .filter(table => table.lng > lng - rLng && table.lng < lng + rLng)
      .map(_.placeIds.unnest)
      .join(placeTable
        .filter(!_.deleted)
        .filter(_.childGuildIds @> subGuildIds)
      )
      .on(_ === _.id)
      .join(rateTable
        .filter(!_.deleted)
      )
      .on(_._2.id === _.placeId)
      .sortBy(x => (x._2.count.desc, x._2.average.desc))
      .map(_._1._2)

    db.run(query.result).map { r =>
      (r, r.length)
    }

  }

  override def getNearPlaces(subGuildIds: List[Long], lat: Double, lng: Double, radio: Double, offset: Int, length: Int): Future[(Seq[Place], Int)] = {

    val rLat = radio * ZoneUtil.ONE_K_EQ_LAT_DEG
    val rLng = radio * (ZoneUtil.ONE_K_EQ_LNG_DEG_DOWN + ZoneUtil.ONE_K_EQ_LNG_DEG_UP) / 2d
    val query = zoneTable
      .filter(!_.disabled)
      .filter(!_.deleted)
      .filter(table => table.lat > lat - rLat && table.lat < lat + rLat)
      .filter(table => table.lng > lng - rLng && table.lng < lng + rLng)
      .sortBy(l => (l.lat - lat) * (l.lat - lat) + (l.lng - lng) * (l.lng - lng))
      .map(_.placeIds.unnest)
      .join(placeTable
        .filter(!_.deleted)
        .filter(_.childGuildIds @> subGuildIds)
      )
      .on(_ === _.id)
      .map(_._2)
      .result

    db.run(query).map { r =>
      (r, r.length)
    }

  }

  override def getQuestions(subGuildId: Long, offset: Int, length: Int): Future[Seq[UserQuestion]] = {

    val query = questionSubGuildTable
      .filter(!_.deleted)
      .filter(_.subGuildId === subGuildId)
      .drop(offset)
      .take(length)
      .join(userQuestionTable
        .filter(!_.deleted)
      )
      .on(_.questionId === _.id)
      .map(_._2)
      .result

    db.run(query)

  }

}
