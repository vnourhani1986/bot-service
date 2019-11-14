package com.bot.telegram

import java.util.UUID

import akka.actor.{Actor, ActorSystem}
import akka.util.Timeout
import com.bot.models.repo.place._
import com.bot.models.repo.review.{UserLike, UserRate, UserReview}
import com.bot.models.repo.user.UserTracking
import com.bot.repos.aggregate.AggregateRepoImpl
import com.bot.repos.city.CityRepoImpl
import com.bot.repos.filter.FilterRepoImpl
import com.bot.repos.guild._3net.{GuildRepoImpl, SubGuildRepoImpl}
import com.bot.repos.place._
import com.bot.repos.review._
import com.bot.repos.user.{UserActivityRepoImpl, UserRepoImpl, UserTrackingRepoImpl}
import com.bot.repos.zone.ZoneRepoImpl
import com.bot.services.telegram.TelegramBotService.Messages._
import com.bot.telegram.PostgresActor.{SaveUser, UpdateUser}
import com.bot.utils.{DateTimeUtils, QueryPlaceUtil, ZoneUtil}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

class PostgresActor(
                     implicit
                     system: ActorSystem,
                     ec: ExecutionContext,
                     timeout: Timeout
                   ) extends Actor with LazyLogging {

  self ! Start()

  override def receive(): Receive = {

    case Start() =>
      logger.info(s"""postgres dispatcher actor is started""")

    case SaveUserInfo(user) =>

      val ref = sender()
      UserRepoImpl.insertOrUpdate(user)
        .map { nrs =>
          if (nrs == 0) {
            self ! SaveUserInfo(user)
          } else {
            ref ! nrs
          }
        }
        .recover {
          case error: Throwable =>
            logger.info(s"""insert or update user in database with error: ${error.getMessage}""")
            self ! SaveUserInfo(user)
        }

    case SaveUser(user) =>
      UserRepoImpl.save(user)
        .recover {
          case error: Throwable =>
            logger.info(s"""save user in database with error: ${error.getMessage}""")
            self ! SaveUser(user)
        }

    case UpdateUser(user) =>
      UserRepoImpl.update(user).map {
        case result if result =>

        case _ =>
          logger.info(s"""update user in database with result false""")
          self ! UpdateUser(user)
      }.recover {
        case error: Throwable =>
          logger.info(s"""update user in database with error: ${error.getMessage}""")
          self ! UpdateUser(user)
      }

    case SaveUserActivity(userActivity) =>

      val ref = sender()
      UserActivityRepoImpl.save(userActivity)
        .map { nrs => ref ! nrs }
        .recover {
          case error: Throwable =>
            logger.info(s"""save user activity in database with error: ${error.getMessage}""")
            self ! SaveUserActivity(userActivity)
        }

    case SaveUserLocation(userId, location) =>

      val ref = sender()
      UserTrackingRepoImpl.save(UserTracking(
        userId = userId,
        lat = location.latitude,
        lng = location.longitude,
        createdAt = DateTimeUtils.now
      )).map(r => ref ! r)


    case SavePlaceLike(userId, placeId, like) =>

      val ref = sender()
      (for {
        oldLike <- LikeRepoImpl.find(userId, placeId)
        newLike <- if (oldLike.isDefined) {
          LikeRepoImpl.update(oldLike.get.copy(
            like = like
          ))
        } else {
          LikeRepoImpl.save(Like(
            userId = userId,
            placeId = placeId,
            like = like,
            createdAt = DateTimeUtils.now
          ))
        }

      } yield {
        ref ! newLike
      }).recover {
        case error: Throwable =>
          logger.info(s"""save place like in database with error: ${error.getMessage}""")
          ref ! None
      }


    case SaveReviewLike(userId, reviewId, like) =>

      val ref = sender()
      (for {
        oldLike <- UserLikeRepoImpl.find(userId, reviewId)
        newLike <- if (oldLike.isDefined) {
          UserLikeRepoImpl.update(oldLike.get.copy(
            like = like
          ))
        } else {
          UserLikeRepoImpl.save(UserLike(
            userId = userId,
            reviewId = reviewId,
            like = like,
            createdAt = DateTimeUtils.now
          ))
        }

      } yield {
        ref ! newLike
      }).recover {
        case error: Throwable =>
          logger.info(s"""save review like in database with error: ${error.getMessage}""")
          ref ! None
      }


    case GetReview(take, placeId, last) =>

      val ref = sender()
      (for {
        oldPlace <- PlaceRepoImpl.findById(placeId)
        _ <- if (oldPlace.isEmpty) Future.failed(new Exception("requested  place is not exist")) else Future.successful("ok")
        placeUUId = oldPlace.get.uuid.getOrElse("")
        review <- if (last) {
          UserReviewRepoImpl.getLast(placeUUId)
        } else {
          UserReviewRepoImpl.get(take, placeUUId)
        }
      } yield {
        ref ! (review.flatMap(_.id), oldPlace.get.title, review.flatMap(_.content))
      }).recover {
        case error: Throwable =>
          logger.info(s"""get review from database with error: ${error.getMessage}""")
          ref ! (None, None)
      }

    case GetTotalReviews(placeId) =>

      val ref = sender()
      (for {
        oldPlace <- PlaceRepoImpl.findById(placeId)
        _ <- if (oldPlace.isEmpty) Future.failed(new Exception("requested  place is not exist")) else Future.successful("ok")
        placeUUId = oldPlace.get.uuid.getOrElse("")
        totalReviews <- UserReviewRepoImpl.getTotalReviews(placeUUId)
      } yield {
        ref ! totalReviews
      }).recover {
        case error: Throwable =>
          logger.info(s"""get review from database with error: ${error.getMessage}""")
          ref ! 0
      }

    case GetReviewTotalLikes(reviewId) =>

      val ref = sender()
      (for {
        reviewLikes <- UserLikeRepoImpl.find(reviewId)
        likes = reviewLikes.foldRight(0)((l, s) => if (l.like) s + 1 else s)
        dislikes = reviewLikes.foldRight(0)((l, s) => if (!l.like) s + 1 else s)
      } yield {
        ref ! (likes, dislikes)
      }).recover {
        case error: Throwable =>
          logger.info(s"""get review from database with error: ${error.getMessage}""")
          ref ! (0, 0)
      }


    case SaveReview(userId, placeId, comment) =>

      val ref = sender()
      (for {
        oldPlace <- PlaceRepoImpl.findById(placeId)
        _ <- if (oldPlace.isEmpty) Future.failed(new Exception("requested  place is not exist")) else Future.successful("ok")
        placeUUId = oldPlace.get.uuid.getOrElse("")
        newReview <- UserReviewRepoImpl.save(UserReview(
          reviewId = 0l,
          userId = userId,
          placeUUID = placeUUId,
          content = Some(comment)
        ))
      } yield {
        ref ! newReview
      }).recover {
        case error: Throwable =>
          logger.info(s"""save review to database with error: ${error.getMessage}""")
          ref ! None
      }

    case SaveRate(userId, placeId, questionId, rate) =>

      val ref = sender()
      (for {
        oldPlace <- PlaceRepoImpl.findById(placeId)
        _ <- if (oldPlace.isEmpty) Future.failed(new Exception("requested  place is not exist")) else Future.successful("ok")
        placeUUId = oldPlace.get.uuid.getOrElse("")
        oldUserRate <- UserRateRepoImpl.find(userId, placeUUId)
        ids <- if (oldUserRate.isDefined) {
          val (questionIds, questionValues) = (oldUserRate.get.questionIds.zip(oldUserRate.get.questionValues).filter(_._1 != questionId) ++ List((questionId, rate))).sortBy(_._1).unzip
          val average = questionValues.foldRight(0d)((qv, sum) => sum + qv.toDouble) / questionValues.length.toDouble
          for {
            userRateId <- UserRateRepoImpl.update(oldUserRate.get.copy(
              questionIds = questionIds,
              questionValues = questionValues,
              average = Some(average),
              modifiedAt = DateTimeUtils.nowOpt
            ))
            place <- PlaceRepoImpl.findByUUID(placeUUId)
            placeRate <- RateRepoImpl.find(place.flatMap(_.id).get)
            placeRateId <- if (placeRate.isDefined) {
              RateRepoImpl.update(placeRate.map(_.copy(
                average = placeRate.map { r =>
                  (r.average.getOrElse(0d) * r.count.getOrElse(0) - oldUserRate.flatMap(_.average).getOrElse(0d) + average) / r.count.get
                })
              ).get)
            } else {
              RateRepoImpl.update(Rate(
                placeId = Some(placeId),
                average = Some(average),
                count = Some(1),
                createdAt = DateTimeUtils.now
              ))
            }
          } yield {
            (userRateId, placeRateId)
          }
        } else {
          for {
            userRateId <- UserRateRepoImpl.save(UserRate(
              userId = userId,
              placeUUID = placeUUId,
              questionIds = List(questionId),
              questionValues = List(rate),
              average = Some(rate.toDouble),
              createdAt = DateTimeUtils.now
            ))
            place <- PlaceRepoImpl.findByUUID(placeUUId)
            placeRate <- RateRepoImpl.find(place.flatMap(_.id).get)
            placeRateId <- if (placeRate.isDefined) {
              RateRepoImpl.update(placeRate.map(_.copy(
                average = placeRate.map { r =>
                  (r.average.getOrElse(0d) * r.count.getOrElse(0) - oldUserRate.flatMap(_.average).getOrElse(0d) + rate.toDouble) / r.count.get
                })
              ).get)
            } else {
              RateRepoImpl.update(Rate(
                placeId = Some(placeId),
                average = Some(rate.toDouble),
                count = Some(1),
                createdAt = DateTimeUtils.now
              ))
            }
          } yield {
            (userRateId, placeRateId)
          }

        }

      } yield {
        ref ! ids
      }).recover {
        case error: Throwable =>
          logger.info(s"""save rate to database with error: ${error.getMessage}""")
          ref ! (None, None)
      }

    case GetPlaceIdsFromZone(lat, lng, radio, offset, length) =>

      val ref = sender()
      ZoneRepoImpl.get(lat, lng, radio, offset, length).map { placesIdsAndLength =>
        ref ! placesIdsAndLength
      }.recover {
        case error: Throwable =>
          logger.info(s"""get place ids from database with error: ${error.getMessage}""")
          ref ! (Nil, 0)
      }

    case GetNearPlaces(subGuilds, lat, lng, radio, offset, length) =>

      val ref = sender()
      AggregateRepoImpl.getNearPlaces(subGuilds, lat, lng, radio, offset, length).map { tPlaceLength =>
        ref ! tPlaceLength
      }.recover {
        case error: Throwable =>
          logger.info(s"""get places from database with error: ${error.getMessage}""")
          ref ! (Nil, 0)
      }

    case GetPlaceToPostgresActor(placeId) =>

      val ref = sender()
      PlaceRepoImpl.getByIds(Seq(placeId)).map { places =>
        ref ! places
      }.recover {
        case error: Throwable =>
          logger.info(s"""get places from database with error: ${error.getMessage}""")
          ref ! Nil
      }

    case AddPlaceToPostgresActor(userId, cityId, subGuildId, title, address, phone, lat, lng, link) =>

      val ref = sender()
      val place = Place(
        cityId = cityId,
        childGuildIds = List(subGuildId),
        uuid = Some(UUID.randomUUID().toString),
        title = Some(title),
        address = Some(address),
        phone = Some(phone),
        lat = Some(lat.toDouble),
        lng = Some(lng.toDouble),
        links = Some(Link(
          self = link,
          self2 = link
        )),
        createdAt = DateTimeUtils.now,
        provider = Some("3net-by-user"),
        deleted = true
      )

      (for {
        placeId <- PlaceRepoImpl.save(place)
        (userPlaceId, zoneId, queryPlaceId) <- placeId match {
          case Some(id) =>
            val userPlace = UserPlace(
              userId = userId,
              placeId = id,
              createdAt = DateTimeUtils.now
            )
            for {
              pId <- UserPlaceRepoImpl.save(userPlace)
              zId <- ZoneUtil.updateZoneTable(id, lat.toDouble, lng.toDouble)
              qpId <- QueryPlaceUtil.updateQueryPlaceTable(place.copy(id = placeId))
            } yield {
              (pId, zId, qpId)
            }

          case None => Future.failed(new Exception("postgres actor could not save place in database"))
        }
        _ <- userPlaceId match {
          case Some(upId) => Future.successful(upId)
          case None => Future.failed(new Exception("postgres actor could not save user place in database"))
        }
      } yield {
        ref ! (placeId, userPlaceId, queryPlaceId)
      }).recover {
        case error: Throwable =>
          logger.info(s"""save place to database with error: ${error.getMessage}""")
          ref ! (None, None, None)
      }

    case QueryFromUserPlace(userId, query) =>

      val ref = sender()
      (for {
        userPlaces <- UserPlaceRepoImpl.find(userId)
        placeIds = userPlaces.map(_.placeId)
        places <- PlaceRepoImpl.getByQueryIds(placeIds, query)
      } yield {
        ref ! places
      }).recover {
        case error: Throwable =>
          logger.info(s"""get places from database with error: ${error.getMessage}""")
          ref ! Nil
      }

    case UpdatePlace(placeId, title, address, phone, location, cityId, subGuildId, link) =>

      val ref = sender()
      (for {
        placeOpt <- PlaceRepoImpl.findById(placeId)
        (placeId, zoneId, queryPlaceId) <- placeOpt match {
          case Some(place) =>
            val p = place.copy(
              title = title.orElse(place.title),
              address = address.orElse(place.address),
              phone = phone.orElse(place.phone),
              lat = location.map(_.latitude.toDouble).orElse(place.lat),
              lng = location.map(_.longitude.toDouble).orElse(place.lng),
              cityId = cityId.getOrElse(place.cityId),
              childGuildIds = subGuildId.map(sg => (List(sg) ++ place.childGuildIds).distinct).getOrElse(place.childGuildIds),
              links = place.links.map(_.copy(
                self = link.orElse(place.links.flatMap(_.self)),
                self2 = link.orElse(place.links.flatMap(_.self2))
              ))
            )
            for {
              pId <- PlaceRepoImpl.update(p)
              zId <- ZoneUtil.updateZoneTable(p.id.get, location.map(_.latitude.toDouble).getOrElse(0d), location.map(_.longitude.toDouble).getOrElse(0d))
              qpId <- QueryPlaceUtil.updateQueryPlaceTable(p)
            } yield {
              (pId, zId, qpId)
            }

          case None =>
            Future.failed(new Exception("requested place dose not exited"))
        }
      } yield {
        ref ! (placeId, zoneId, queryPlaceId)
      }).recover {
        case error: Throwable =>
          logger.info(s"""update places to database with error: ${error.getMessage}""")
          ref ! (None, None, None)
      }

    case GetBySubGuildAndIds(placeIds, subGuilds) =>

      val ref = sender()
      PlaceRepoImpl.getBySubGuildAndIds(placeIds, subGuilds).map { places =>
        ref ! places
      }.recover {
        case error: Throwable =>
          logger.info(s"""get place ids from database with error: ${error.getMessage}""")
          ref ! Nil
      }

    case GetPopPlaceBySubGuild(subGuilds, lat, lng, radio) =>

      val ref = sender()
      AggregateRepoImpl.getPopPlaces(subGuilds, lat, lng, radio).map { places =>
        ref ! places
      }.recover {
        case error: Throwable =>
          logger.info(s"""get place ids from database with error: ${error.getMessage}""")
          ref ! (Nil, 0)
      }

    case FindPlacesWithLikes(places) =>

      val ref = sender()
      Future.sequence(places.map { p =>
        LikeRepoImpl.find(p.id.get).map { ls =>
          val likes = ls.foldRight(0)((l, s) => if (l.like) s + 1 else s)
          val dislikes = ls.foldRight(0)((l, s) => if (!l.like) s + 1 else s)
          (p, likes, dislikes)
        }
      }).map { r =>
        ref ! r
      }.recover {
        case error: Throwable =>
          logger.info(s"""get place with like from database with error: ${error.getMessage}""")
          ref ! Nil
      }

    case FindSubGuildsByInlineQuery(query) =>

      val ref = sender()
      SubGuildRepoImpl.findByQuery(query, 5).map { guilds =>
        ref ! guilds
      }


    case FindPlaceByQuery(query) =>

      val ref = sender()
      QueryPlaceRepoImpl.find(query).map { qps =>
        ref ! qps
      }.recover {
        case error: Throwable =>
          logger.info(s"""get place ids from database with error: ${error.getMessage}""")
          ref ! Nil
      }

    case FindPlaceByApiQuery(query, lat, lng ,page) =>

      val ref = sender()
      QueryPlaceRepoImpl.find(query, lat, lng, page).map { qps =>
        ref ! qps
      }.recover {
        case error: Throwable =>
          logger.info(s"""get place ids from database with error: ${error.getMessage}""")
          ref ! Nil
      }

    case GetPlaces(placeIds) =>

      val ref = sender()
      PlaceRepoImpl.getByIds(placeIds).map { qs =>
        ref ! qs
      }.recover {
        case error: Throwable =>
          logger.info(s"""get place ids from database with error: ${error.getMessage}""")
          ref ! Nil
      }

    case GetPlaceFromPostgresActor(placeId) =>

      val ref = sender()
      PlaceRepoImpl.findById(placeId).map { place =>
        ref ! place
      }.recover {
        case error: Throwable =>
          logger.info(s"""get place from database with error: ${error.getMessage}""")
          ref ! None
      }

    case GetLastActivity(userId, noAction) =>

      val ref = sender()
      UserActivityRepoImpl.getLast(userId, noAction).map { activity =>
        ref ! activity
      }.recover {
        case error: Throwable =>
          logger.info(s"""get last activity from database with error: ${error.getMessage}""")
          ref ! None
      }

    case GetLastActivityForSpecificAction(userId, action) =>

      val ref = sender()
      UserActivityRepoImpl.getLast(userId, Some(action)).map { activity =>
        ref ! activity
      }.recover {
        case error: Throwable =>
          logger.info(s"""get last activity from database with error: ${error.getMessage}""")
          ref ! None
      }

    case GetLastLocation(userId) =>

      val ref = sender()
      UserTrackingRepoImpl.get(userId).map { track =>
        ref ! track
      }.recover {
        case error: Throwable =>
          logger.info(s"""get last location from database with error: ${error.getMessage}""")
          ref ! None
      }

    case GetGuilds() =>

      val ref = sender()
      GuildRepoImpl.get.map { guilds =>
        ref ! guilds
      }

    case QueryFromCity(query) =>

      val ref = sender()
      CityRepoImpl.find(query).map { cities =>
        ref ! cities
      }.recover {
        case error: Throwable =>
          logger.info(s"""get cities from database with error: ${error.getMessage}""")
          ref ! Nil
      }

    case QueryFromSubGuild(query) =>

      val ref = sender()
      SubGuildRepoImpl.findByQuery(query).map { subGuilds =>
        ref ! subGuilds
      }.recover {
        case error: Throwable =>
          logger.info(s"""get cities from database with error: ${error.getMessage}""")
          ref ! Nil
      }

    case GetSubGuilds(guildId) =>

      val ref = sender()
      SubGuildRepoImpl.find(guildId).map { subGuilds =>
        ref ! subGuilds
      }

    case GetSubGuildId(title) =>

      val ref = sender()
      SubGuildRepoImpl.find(title).map { subGuild =>
        ref ! subGuild.flatMap(_.id)
      }.recover {
        case error: Throwable =>
          logger.info(s"""get sub guild id from database with error: ${error.getMessage}""")
          ref ! None
      }

    case GetCityId(title) =>

      val ref = sender()
      CityRepoImpl.findFaName(title).map { city =>
        ref ! city.flatMap(_.id)
      }.recover {
        case error: Throwable =>
          logger.info(s"""get city id from database with error: ${error.getMessage}""")
          ref ! None
      }

    case GetFilters() =>

      val ref = sender()
      FilterRepoImpl.get.map { filters =>
        ref ! filters
      }

    case GetQuestions(subGuildId, offset, length) =>

      val ref = sender()
      for {
        questions <- AggregateRepoImpl.getQuestions(subGuildId, offset, length)
      } yield {
        ref ! questions
      }

    case _ =>
      logger.info(s"""welcome to postgres dispatcher actor""")

  }

}

object PostgresActor {

  import com.bot.models.repo.user._

  case class SaveUser(user: User)

  case class UpdateUser(user: User)


}