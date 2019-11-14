package com.bot.telegram

import akka.actor.{Actor, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.bot.formats.Formats._
import com.bot.models.json.callback.Param
import com.bot.models.repo.filter.Filter
import com.bot.models.repo.user.{User, UserActivity, UserTracking}
import com.bot.repos.user.UserActivityRepoImpl
import com.bot.services.telegram.TelegramBotService.Messages._
import com.bot.services.telegram.TelegramBotService.{addPlaceRouterActor, callbackDispatcherActor, commandActor, editPlaceActor, elasticSearchActor, findPlaceActor, inlineDispatcherActor, postgresActor}
import com.bot.telegram.CallbackDispatcherActor.FromButton
import com.bot.telegram.CommandActor.Commands
import com.bot.telegram.ElasticSearchActor.{ElasticUserTracking, GeoPoint}
import com.bot.telegram.UserActor._
import com.bot.utils.{DateTimeUtils, TelegramBotConfig}
import com.typesafe.scalalogging.LazyLogging
import spray.json.{JsValue, _}

import scala.concurrent.{ExecutionContext, Future}

class UserActor(
                 implicit
                 system: ActorSystem,
                 ec: ExecutionContext,
                 timeout: Timeout
               ) extends Actor with LazyLogging {

  self ! Start()

  override def receive(): Receive = {

    case Start() =>
      logger.info(s"""user actor is started""")

    case SendCommandToUserActor(chat, user, command) =>

      (for {
        action <- if (command == Commands.START) {
          Future.successful(Action.START_COMMAND)
        } else if (command == Commands.FIND_PLACE) {
          Future.successful(Action.SET_FIND_PLACE)
        } else if (command == Commands.ADD_PLACE) {
          Future.successful(Action.SET_ADD_PLACE)
        } else if (command == Commands.EDIT_PLACE) {
          Future.successful(Action.SET_EDIT_PLACE)
        } else if (command == Commands.EDIT_PLACE_TITLE) {
          Future.successful(Action.EDIT_PLACE_TITLE)
        } else if (command == Commands.EDIT_PLACE_ADDRESS) {
          Future.successful(Action.EDIT_PLACE_ADDRESS)
        } else if (command == Commands.EDIT_PLACE_PHONE) {
          Future.successful(Action.EDIT_PLACE_PHONE)
        } else if (command == Commands.EDIT_PLACE_LOCATION) {
          Future.successful(Action.EDIT_PLACE_LOCATION)
        } else if (command == Commands.EDIT_PLACE_CITY_TITLE) {
          Future.successful(Action.EDIT_PLACE_CITY_ID)
        } else if (command == Commands.EDIT_PLACE_SUB_GUILD_TITLE) {
          Future.successful(Action.EDIT_PLACE_SUB_GUILD_ID)
        } else if (command == Commands.EDIT_PLACE_LINK) {
          Future.successful(Action.EDIT_PLACE_LINK)
        } else {
          Future.successful(Action.SET_FIND_PLACE)
        }

        _ <- if (user.isDefined) {
          for {
            r1 <- (self ? SaveUserWithUserInfo(user.get)).mapTo[Int]
            _ <- (self ? SaveUserActivityWithUserInfo(user.get, chat, action, None)).mapTo[UserActivity]
          } yield {
            if (r1 > 1) logger.info(s"""save user info or user activity in database with error: more than one row updated""")
          }
        } else {
          for {
            r1 <- (self ? SaveUserWithChatInfo(chat)).mapTo[Int]
            _ <- (self ? SaveUserActivityWithChatInfo(chat, action, None)).mapTo[UserActivity]
          } yield {
            if (r1 > 1) logger.info(s"""save user info or user activity in database with error: more than one row updated""")
          }
        }
      } yield {
        commandActor ! SendCommandToCommandActor(chat, command)
      }).recover {
        case error: Throwable =>
          logger.info(s"""save user info or user activity in database with error: ${error.getMessage}""")
          self ! SendCommandToUserActor(chat, user, command)
      }

    case SendFindPlaceToUserActor(user, metaData) =>

      (for {
        r1 <- (self ? SaveUserWithUserInfo(user)).mapTo[Int]
        _ <- (self ? SaveUserActivityWithUserInfoWithoutChatInfo(user, action = Action.SET_FIND_PLACE, entityId = None, metaData = Some(metaData))).mapTo[UserActivity]
      } yield {
        if (r1 > 1) logger.info(s"""save user info or user activity in database with error: more than one row updated""")
        callbackDispatcherActor ! SendFindPlaceToUserActor(user, metaData)
      }).recover {
        case error: Throwable =>
          logger.info(s"""save user info or user activity in database with error: ${error.getMessage}""")
          self ! SendFindPlaceToUserActor(user, metaData)
      }

    case SendSearchPlaceToUserActor(user, metaData) =>

      (for {
        r1 <- (self ? SaveUserWithUserInfo(user)).mapTo[Int]
        _ <- (self ? SaveUserActivityWithUserInfoWithoutChatInfo(user, action = Action.SET_SEARCH_PLACE, entityId = None, metaData = Some(metaData))).mapTo[UserActivity]
      } yield {
        if (r1 > 1) logger.info(s"""save user info or user activity in database with error: more than one row updated""")
        callbackDispatcherActor ! SendSearchPlaceToUserActor(user, metaData)
      }).recover {
        case error: Throwable =>
          logger.info(s"""save user info or user activity in database with error: ${error.getMessage}""")
          self ! SendSearchPlaceToUserActor(user, metaData)
      }

    case SendAddPlaceToUserActor(user, metaData) =>

      (for {
        r1 <- (self ? SaveUserWithUserInfo(user)).mapTo[Int]
        _ <- (self ? SaveUserActivityWithUserInfoWithoutChatInfo(user, action = Action.SET_ADD_PLACE, entityId = None, metaData = Some(metaData))).mapTo[UserActivity]
      } yield {
        if (r1 > 1) logger.info(s"""save user info or user activity in database with error: more than one row updated""")
        callbackDispatcherActor ! SendAddPlaceToUserActor(user, metaData)
      }).recover {
        case error: Throwable =>
          logger.info(s"""save user info or user activity in database with error: ${error.getMessage}""")
          self ! SendAddPlaceToUserActor(user, metaData)
      }

    case SendEditPlaceToUserActor(user, metaData) =>

      (for {
        r1 <- (self ? SaveUserWithUserInfo(user)).mapTo[Int]
        _ <- (self ? SaveUserActivityWithUserInfoWithoutChatInfo(user, action = Action.SET_EDIT_PLACE, entityId = None, metaData = Some(metaData))).mapTo[UserActivity]
      } yield {
        if (r1 > 1) logger.info(s"""save user info or user activity in database with error: more than one row updated""")
        callbackDispatcherActor ! SendEditPlaceToUserActor(user, metaData)
      }).recover {
        case error: Throwable =>
          logger.info(s"""save user info or user activity in database with error: ${error.getMessage}""")
          self ! SendEditPlaceToUserActor(user, metaData)
      }

    case SendSelectedGuildToUserActor(user, guildId, metaData) =>

      (for {
        r1 <- (self ? SaveUserWithUserInfo(user)).mapTo[Int]
        _ <- (self ? SaveUserActivityWithUserInfoWithoutChatInfo(user, action = Action.SET_GUILD, entityId = Some(guildId), metaData = Some(metaData))).mapTo[UserActivity]
      } yield {
        if (r1 > 1) logger.info(s"""save user info or user activity in database with error: more than one row updated""")
        callbackDispatcherActor ! SendSelectedGuildToUserActor(user, guildId, metaData)
      }).recover {
        case error: Throwable =>
          logger.info(s"""save user info or user activity in database with error: ${error.getMessage}""")
          self ! SendSelectedGuildToUserActor(user, guildId, metaData)
      }

    case SendSelectedSubGuildToUserActor(user, subGuildId, metaData) =>

      (for {
        r1 <- (self ? SaveUserWithUserInfo(user)).mapTo[Int]
        _ <- (self ? SaveUserActivityWithUserInfoWithoutChatInfo(user, action = Action.SET_SUB_GUILD, entityId = Some(subGuildId), metaData = Some(metaData))).mapTo[UserActivity]
      } yield {
        if (r1 > 1) logger.info(s"""save user info or user activity in database with error: more than one row updated""")
        callbackDispatcherActor ! SendSelectedSubGuildToUserActor(user, subGuildId, metaData)
      }).recover {
        case error: Throwable =>
          logger.info(s"""save user info or user activity in database with error: ${error.getMessage}""")
          self ! SendSelectedSubGuildToUserActor(user, subGuildId, metaData)
      }

    case SendSelectedFilterToUserActor(user, subGuildId, filterId, metaData) =>

      (for {
        r1 <- (self ? SaveUserWithUserInfo(user)).mapTo[Int]
        _ <- (self ? SaveUserActivityWithUserInfoWithoutChatInfo(user, action = Action.SET_FILTER, entityId = Some(filterId), metaData = Some(metaData))).mapTo[UserActivity]
      } yield {
        if (r1 > 1) logger.info(s"""save user info or user activity in database with error: more than one row updated""")
        callbackDispatcherActor ! SendSelectedFilterToUserActor(user, subGuildId, filterId, metaData)
      }).recover {
        case error: Throwable =>
          logger.info(s"""save user info or user activity in database with error: ${error.getMessage}""")
          self ! SendSelectedFilterToUserActor(user, subGuildId, filterId, metaData)
      }

    case SendSelectedDistanceFilterToUserActor(user, subGuildId, filterId, distance, metaData) =>

      (for {
        r1 <- (self ? SaveUserWithUserInfo(user)).mapTo[Int]
        _ <- (self ? SaveUserActivityWithUserInfoWithoutChatInfo(user, action = Action.SET_DISTANCE, entityId = None, metaData = Some(metaData))).mapTo[UserActivity]
      } yield {
        if (r1 > 1) logger.info(s"""save user info or user activity in database with error: more than one row updated""")
        callbackDispatcherActor ! SendSelectedDistanceFilterToUserActor(user, subGuildId, filterId, distance, metaData)
      }).recover {
        case error: Throwable =>
          logger.info(s"""save user info or user activity in database with error: ${error.getMessage}""")
          self ! SendSelectedDistanceFilterToUserActor(user, subGuildId, filterId, distance, metaData)
      }

    case SendLocationToUserActor(messageId, chat, user, location) =>

      (for {
        (lastActivity, filters) <- if (user.isDefined) {
          for {
            lastActivity <- UserActivityRepoImpl.getLast(user.get.id)
            r1 <- (self ? SaveUserWithUserInfo(user.get)).mapTo[Int]
            _ <- (self ? SaveUserActivityWithUserInfo(user.get, chat, Action.SET_LOCATION, None)).mapTo[UserActivity]
            userTracking <- (postgresActor ? SaveUserLocation(user.get.id, location)).mapTo[UserTracking]
            elasticUserTracking = convertSysToElasticTracking(userTracking)
            _ = elasticSearchActor ! SendIndexToElasticSearchActor(elasticUserTracking.toJson, s"""/user_tracking_idx/_doc""")
            filters <- (postgresActor ? GetFilters()).mapTo[Seq[Filter]]
          } yield {
            if (r1 > 1) logger.info(s"""save user info or user activity in database with error: more than one row updated""")
            (lastActivity, filters)
          }
        } else {
          for {
            lastActivity <- UserActivityRepoImpl.getLast(chat.id)
            r1 <- (self ? SaveUserWithChatInfo(chat)).mapTo[Int]
            _ <- (self ? SaveUserActivityWithChatInfo(chat, Action.SET_LOCATION, None)).mapTo[UserActivity]
            userTracking <- (postgresActor ? SaveUserLocation(user.get.id, location)).mapTo[UserTracking]
            elasticUserTracking = convertSysToElasticTracking(userTracking)
            _ = elasticSearchActor ! SendIndexToElasticSearchActor(elasticUserTracking.toJson, s"""/user_tracking_idx/_doc""")
            filters <- (postgresActor ? GetFilters()).mapTo[Seq[Filter]]
          } yield {
            if (r1 > 1) logger.info(s"""save user info or user activity in database with error: more than one row updated""")
            (lastActivity, filters)
          }
        }

      } yield {
        val paramOpt = lastActivity.flatMap(_.metaData.map(_.convertTo[Param]))
        val subGuildId = paramOpt.flatMap(_.sgId).getOrElse(0l)
        val distance = paramOpt.flatMap(_.dst).getOrElse(1d)
        lastActivity match {
          case Some(activity) if activity.action == Action.SET_DISTANCE || activity.action == Action.SET_FILTER =>

            filters.find(_.id == activity.metaData.flatMap(_.convertTo[Param].fId)).map(_.data) match {
              case Some(data) if data == "near" => findPlaceActor ! GetNearByDistanceCalc(messageId, user.map(_.id).getOrElse(chat.id), List(subGuildId), location, distance, 1, 10, last = false, from = FromButton.SET_LOCATION)
              case Some(data) if data == "pop" => findPlaceActor ! GetPopPlaces(messageId, user.map(_.id).getOrElse(chat.id), List(subGuildId), location, 10d, 1, 10, last = false, from = FromButton.SET_LOCATION)
              case None => findPlaceActor ! GetNearFromOSRM(messageId, user.map(_.id).getOrElse(chat.id), List(subGuildId), location, distance, 1, 5, last = false, from = FromButton.SET_LOCATION)
            }

          case Some(activity) if activity.action == Action.SET_PLACE_PHONE =>

            addPlaceRouterActor ! SendLocationToAddPlaceActor(chat, user, location)

          case Some(activity) if activity.action == Action.EDIT_PLACE_LOCATION =>

            editPlaceActor ! SendLocationToEditPlaceActor(chat, user, location)

          case Some(activity) =>

          case None =>
        }
      }).recover {
        case error: Throwable =>
          logger.info(s"""save user info or user activity in database with error: ${error.getMessage}""")
          self ! SendLocationToUserActor(messageId, chat, user, location)
      }

    case SendOtherPagePlaceListToUserActor(messageId, user, metaData, page, last, action, from) =>

      (for {
        r1 <- (self ? SaveUserWithUserInfo(user)).mapTo[Int]
        _ <- (self ? SaveUserActivityWithUserInfoWithoutChatInfo(user, action = action, entityId = None, metaData = Some(metaData))).mapTo[UserActivity]
      } yield {
        if (r1 > 1) logger.info(s"""save user info or user activity in database with error: more than one row updated""")
        callbackDispatcherActor ! SendOtherPagePlaceListToUserActor(messageId, user, metaData, page, last, action, from)
      }).recover {
        case error: Throwable =>
          logger.info(s"""save user info or user activity in database with error: ${error.getMessage}""")
          self ! SendOtherPagePlaceListToUserActor(messageId, user, metaData, page, last, action, from)
      }

    case SendSelectedPlaceToUserActor(messageId, user, placeId, metaData, action, from) =>

      (for {
        r1 <- (self ? SaveUserWithUserInfo(user)).mapTo[Int]
        _ <- (self ? SaveUserActivityWithUserInfoWithoutChatInfo(user, action = action, entityId = Some(placeId), metaData = Some(metaData))).mapTo[UserActivity]
      } yield {
        if (r1 > 1) logger.info(s"""save user info or user activity in database with error: more than one row updated""")
        callbackDispatcherActor ! SendSelectedPlaceToUserActor(messageId, user, placeId, metaData, action, from)
      }).recover {
        case error: Throwable =>
          logger.info(s"""save user info or user activity in database with error: ${error.getMessage}""")
          self ! SendSelectedPlaceToUserActor(messageId, user, placeId, metaData, action, from)
      }


    case SendPlaceLikeToUserActor(user, placeId, like, metaData) =>

      (for {
        r1 <- (self ? SaveUserWithUserInfo(user)).mapTo[Int]
        _ <- (self ? SaveUserActivityWithUserInfoWithoutChatInfo(user, action = if (like) Action.SET_PLACE_LIKE else Action.SET_PLACE_DISLIKE, entityId = Some(placeId), metaData = Some(metaData))).mapTo[UserActivity]
        _ <- (postgresActor ? SavePlaceLike(user.id, placeId, like)).mapTo[Option[Long]]
      } yield {
        if (r1 > 1) logger.info(s"""save user info or user activity in database with error: more than one row updated""")
        //        callbackDispatcherActor ! SendPlaceLikeToUserActor(user, placeId, like, metaData)
      }).recover {
        case error: Throwable =>
          logger.info(s"""save user info or user activity in database with error: ${error.getMessage}""")
          self ! SendPlaceLikeToUserActor(user, placeId, like, metaData)
      }

    case SendSelectedPointToUserActor(messageId, user, placeId, metaData, page, last, action, from) =>

      (for {
        r1 <- (self ? SaveUserWithUserInfo(user)).mapTo[Int]
        _ <- (self ? SaveUserActivityWithUserInfoWithoutChatInfo(user, action = action, entityId = Some(placeId), metaData = Some(metaData))).mapTo[UserActivity]
      } yield {
        if (r1 > 1) logger.info(s"""save user info or user activity in database with error: more than one row updated""")
        callbackDispatcherActor ! SendSelectedPointToUserActor(messageId, user, placeId, metaData, page, last, action, from)
      }).recover {
        case error: Throwable =>
          logger.info(s"""save user info or user activity in database with error: ${error.getMessage}""")
          self ! SendSelectedPointToUserActor(messageId, user, placeId, metaData, page, last, action, from)
      }

    case SendShowReviewToUserActor(messageId, user, placeId, page, metaData, last, action, from) =>

      (for {
        r1 <- (self ? SaveUserWithUserInfo(user)).mapTo[Int]
        _ <- (self ? SaveUserActivityWithUserInfoWithoutChatInfo(user, action = action, entityId = None, metaData = Some(metaData))).mapTo[UserActivity]
      } yield {
        if (r1 > 1) logger.info(s"""save user info or user activity in database with error: more than one row updated""")
        callbackDispatcherActor ! SendShowReviewToUserActor(messageId, user, placeId, page, metaData, last, action, from)
      }).recover {
        case error: Throwable =>
          logger.info(s"""save user info or user activity in database with error: ${error.getMessage}""")
          self ! SendShowReviewToUserActor(messageId, user, placeId, page, metaData, last, action, from)
      }

    case SendReviewLikeToUserActor(user, reviewId, like, metaData) =>

      (for {
        r1 <- (self ? SaveUserWithUserInfo(user)).mapTo[Int]
        _ <- (self ? SaveUserActivityWithUserInfoWithoutChatInfo(user, action = if (like) Action.SET_REVIEW_LIKE else Action.SET_REVIEW_DISLIKE, entityId = Some(reviewId), metaData = Some(metaData))).mapTo[UserActivity]
        _ <- (postgresActor ? SaveReviewLike(user.id, reviewId, like)).mapTo[Option[Long]]
      } yield {
        if (r1 > 1) logger.info(s"""save user info or user activity in database with error: more than one row updated""")
        //        callbackDispatcherActor ! SendPlaceLikeToUserActor(user, placeId, like, metaData)
      }).recover {
        case error: Throwable =>
          logger.info(s"""save user info or user activity in database with error: ${error.getMessage}""")
          self ! SendReviewLikeToUserActor(user, reviewId, like, metaData)
      }


    case SendSetReviewToUserActor(user, placeId, metaData) =>

      (for {
        r1 <- (self ? SaveUserWithUserInfo(user)).mapTo[Int]
        _ <- (self ? SaveUserActivityWithUserInfoWithoutChatInfo(user, action = Action.GO_TO_PLACE_SET_REVIEW, entityId = Some(placeId), metaData = Some(metaData))).mapTo[UserActivity]
      } yield {
        if (r1 > 1) logger.info(s"""save user info or user activity in database with error: more than one row updated""")
        callbackDispatcherActor ! SendSetReviewToUserActor(user, placeId, metaData)
      }).recover {
        case error: Throwable =>
          logger.info(s"""save user info or user activity in database with error: ${error.getMessage}""")
          self ! SendSetReviewToUserActor(user, placeId, metaData)
      }

    case SendTextToUserActor(chat, user, text) =>

      (for {
        (userId, reviewId) <- if (user.isDefined) {
          for {
            r1 <- (self ? SaveUserWithUserInfo(user.get)).mapTo[Int]
            lastActivity <- (postgresActor ? GetLastActivity(user.get.id)).mapTo[Option[UserActivity]]
            reviewId <- lastActivity match {
              case Some(activity) if activity.action == Action.GO_TO_PLACE_SET_REVIEW =>

                for {
                  _ <- Future.successful()
                  paramOpt = lastActivity.flatMap(_.metaData.map(_.convertTo[Param]))
                  placeId = paramOpt.flatMap(_.pId).getOrElse(0l)
                  rId <- (postgresActor ? SaveReview(user.get.id, placeId, text)).mapTo[Option[Long]]
                  _ <- (self ? SaveUserActivityWithUserInfo(user.get, chat, Action.SET_PLACE_REVIEW, rId)).mapTo[UserActivity]
                } yield {
                  rId
                }

              case Some(activity) if activity.action == Action.START_ADD_PLACE ||
                activity.action == Action.SET_PLACE_TITLE ||
                activity.action == Action.SET_PLACE_ADDRESS ||
                activity.action == Action.SET_PLACE_SUB_GUILD_ID =>

                addPlaceRouterActor ! SendTextToAddPlaceActor(chat, user, text, lastActivity.map(_.action).getOrElse(""))
                Future.successful(Some(-1l))

              case Some(activity) if activity.action == Action.EDIT_PLACE_TITLE ||
                activity.action == Action.EDIT_PLACE_ADDRESS ||
                activity.action == Action.EDIT_PLACE_PHONE ||
                activity.action == Action.EDIT_PLACE_LINK =>

                editPlaceActor ! SendTextToEditPlaceActor(chat, user, text, lastActivity.map(_.action).getOrElse(""))
                Future.successful(Some(-1l))

              case None =>

                Future.successful(Some(-1l))

            }
          } yield {
            if (r1 > 1) logger.info(s"""save user info or user activity in database with error: more than one row updated""")
            (user.get.id, reviewId)
          }
        } else {
          for {
            r1 <- (self ? SaveUserWithChatInfo(chat)).mapTo[Int]
            lastActivity <- (postgresActor ? GetLastActivity(chat.id)).mapTo[Option[UserActivity]]
            reviewId <- lastActivity match {
              case Some(activity) if activity.action == Action.GO_TO_PLACE_SET_REVIEW =>

                for {
                  _ <- Future.successful()
                  paramOpt = lastActivity.flatMap(_.metaData.map(_.convertTo[Param]))
                  placeId = paramOpt.flatMap(_.pId).getOrElse(0l)
                  rId <- (postgresActor ? SaveReview(user.get.id, placeId, text)).mapTo[Option[Long]]
                  _ <- (self ? SaveUserActivityWithUserInfo(user.get, chat, Action.SET_PLACE_REVIEW, rId)).mapTo[UserActivity]
                } yield {
                  rId
                }

              case Some(activity) if activity.action == Action.START_ADD_PLACE ||
                activity.action == Action.SET_PLACE_TITLE ||
                activity.action == Action.SET_PLACE_ADDRESS ||
                activity.action == Action.SET_PLACE_SUB_GUILD_ID =>

                addPlaceRouterActor ! SendTextToAddPlaceActor(chat, user, text, lastActivity.map(_.action).getOrElse(""))
                Future.successful(Some(-1l))

              case Some(activity) if activity.action == Action.EDIT_PLACE_TITLE ||
                activity.action == Action.EDIT_PLACE_ADDRESS ||
                activity.action == Action.EDIT_PLACE_PHONE ||
                activity.action == Action.EDIT_PLACE_LINK =>

                editPlaceActor ! SendTextToEditPlaceActor(chat, user, text, lastActivity.map(_.action).getOrElse(""))
                Future.successful(Some(-1l))

              case None =>

                Future.successful(Some(-1l))

            }
          } yield {
            if (r1 > 1) logger.info(s"""save user info or user activity in database with error: more than one row updated""")
            (chat.id, reviewId)
          }
        }

      } yield {

        reviewId match {
          case Some(id) if id > 0l =>
            callbackDispatcherActor ! CreateBackToMainMenu(userId, TelegramBotConfig.successPhoto, REVIEW_SAVED)
          case None =>
            callbackDispatcherActor ! CreateBackToMainMenu(userId, TelegramBotConfig.successPhoto, REVIEW_NOT_SAVED)
        }

      }).recover {
        case error: Throwable =>
          logger.info(s"""save user info or user activity in database with error: ${error.getMessage}""")
      }

    case SendSelectedPointItemToUserActor(user, placeId, questionId, rate, metaData) =>

      (for {
        r1 <- (self ? SaveUserWithUserInfo(user)).mapTo[Int]
        _ <- (self ? SaveUserActivityWithUserInfoWithoutChatInfo(user, action = Action.SET_PLACE_POINT, entityId = Some(questionId), metaData = Some(metaData))).mapTo[UserActivity]
        _ <- (postgresActor ? SaveRate(user.id, placeId, questionId, rate)).mapTo[(Option[Long], Option[Long])]
      } yield {
        if (r1 > 1) logger.info(s"""save user info or user activity in database with error: more than one row updated""")
        //        callbackDispatcherActor ! SendSelectedPointItemToUserActor(user, placeId, questionId, rate, metaData)
      }).recover {
        case error: Throwable =>
          logger.info(s"""save user info or user activity in database with error: ${error.getMessage}""")
          self ! SendSelectedPointItemToUserActor(user, placeId, questionId, rate, metaData)
      }

    case SendQueryToUserActor(inlineQuery) =>

      (for {
        r1 <- (self ? SaveUserWithUserInfo(inlineQuery.from)).mapTo[Int]
        lastActivity <- (postgresActor ? GetLastActivity(inlineQuery.from.id, Action.GET_INLINE_QUERY)).mapTo[Option[UserActivity]]
        _ <- (self ? SaveUserActivityWithUserInfoWithoutChatInfo(inlineQuery.from, action = Action.GET_INLINE_QUERY, entityId = Some(inlineQuery.id.toLong), metaData = Some(inlineQuery.toJson))).mapTo[UserActivity]
      } yield {
        if (r1 > 1) logger.info(s"""save user info or user activity in database with error: more than one row updated""")
        lastActivity.map(_.action) match {

          case Some(action) if action == Action.SET_PLACE_LOCATION => inlineDispatcherActor ! InlineQueryFromCity(inlineQuery)
          case Some(action) if action == Action.SET_PLACE_CITY_ID => inlineDispatcherActor ! InlineQueryFromSubGuild(inlineQuery)
          case Some(action) if action == Action.SET_EDIT_PLACE => inlineDispatcherActor ! InlineQueryFromUserPlace(inlineQuery)
          case Some(action) if action == Action.EDIT_PLACE_CITY_ID => inlineDispatcherActor ! InlineQueryFromCity(inlineQuery)
          case Some(action) if action == Action.EDIT_PLACE_SUB_GUILD_ID => inlineDispatcherActor ! InlineQueryFromSubGuild(inlineQuery)
          case Some(_) => inlineDispatcherActor ! InlineQueryFromPlace(inlineQuery)
          case None => inlineDispatcherActor ! InlineQueryFromPlace(inlineQuery)

        }

      }).recover {
        case error: Throwable =>
          logger.info(s"""save user info or user activity in database with error: ${error.getMessage}""")
          self ! SendQueryToUserActor(inlineQuery)
      }

    case SendChosenInlineResultToUserActor(chosenInlineResult) =>

      (for {
        r1 <- (self ? SaveUserWithUserInfo(chosenInlineResult.from)).mapTo[Int]
        lastActivity <- (postgresActor ? GetLastActivity(chosenInlineResult.from.id, Action.GET_INLINE_QUERY)).mapTo[Option[UserActivity]]
        _ <- lastActivity.map(_.action) match {
          case Some(action) if action == Action.SET_PLACE_LOCATION =>

            inlineDispatcherActor ! SendCityToDispatcherActor(chosenInlineResult)
            (self ? SaveUserActivityWithUserInfoWithoutChatInfo(chosenInlineResult.from, action = Action.SET_PLACE_CITY_ID, entityId = Some(chosenInlineResult.result_id.toLong), metaData = Some(chosenInlineResult.toJson))).mapTo[UserActivity]

          case Some(action) if action == Action.SET_PLACE_CITY_ID =>

            inlineDispatcherActor ! SendSubGuildToDispatcherActor(chosenInlineResult)
            (self ? SaveUserActivityWithUserInfoWithoutChatInfo(chosenInlineResult.from, action = Action.SET_PLACE_SUB_GUILD_ID, entityId = Some(chosenInlineResult.result_id.toLong), metaData = Some(chosenInlineResult.toJson))).mapTo[UserActivity]

          case Some(action) if action == Action.EDIT_PLACE_CITY_ID =>

            inlineDispatcherActor ! SendEditPlaceCityToDispatcherActor(chosenInlineResult)
            //            (self ? SaveUserActivityWithUserInfoWithoutChatInfo(chosenInlineResult.from, action = Action.EDITED_PLACE_CITY_ID, entityId = Some(chosenInlineResult.result_id.toLong), metaData = Some(chosenInlineResult.toJson))).mapTo[UserActivity]
            Future.successful()

          case Some(action) if action == Action.EDIT_PLACE_SUB_GUILD_ID =>

            inlineDispatcherActor ! SendEditPlaceSubGuildToDispatcherActor(chosenInlineResult)
            //            (self ? SaveUserActivityWithUserInfoWithoutChatInfo(chosenInlineResult.from, action = Action.EDITED_PLACE_SUB_GUILD_ID, entityId = Some(chosenInlineResult.result_id.toLong), metaData = Some(chosenInlineResult.toJson))).mapTo[UserActivity]
            Future.successful()

          case Some(action) if action == Action.SET_EDIT_PLACE =>

            inlineDispatcherActor ! SendPlaceToDispatcherActor(chosenInlineResult)
            (self ? SaveUserActivityWithUserInfoWithoutChatInfo(chosenInlineResult.from, action = Action.START_EDIT_PLACE, entityId = Some(chosenInlineResult.result_id.toLong), metaData = Some(chosenInlineResult.toJson))).mapTo[UserActivity]

          case Some(_) =>

            (self ? SaveUserActivityWithUserInfoWithoutChatInfo(chosenInlineResult.from, action = Action.GET_PLACE_INLINE_RESULT, entityId = None, metaData = Some(chosenInlineResult.toJson))).mapTo[UserActivity]

          case None =>

            (self ? SaveUserActivityWithUserInfoWithoutChatInfo(chosenInlineResult.from, action = Action.GET_PLACE_INLINE_RESULT, entityId = None, metaData = Some(chosenInlineResult.toJson))).mapTo[UserActivity]

        }
      } yield {
        if (r1 > 1) logger.info(s"""save user info or user activity in database with error: more than one row updated""")
      }).recover {
        case error: Throwable =>
          logger.info(s"""save user info or user activity in database with error: ${error.getMessage}""")
          self ! SendChosenInlineResultToUserActor(chosenInlineResult)
      }

    case SendStartAddPlaceToUserActor(user) =>

      (for {
        r1 <- (self ? SaveUserWithUserInfo(user)).mapTo[Int]
        _ <- (self ? SaveUserActivityWithUserInfoWithoutChatInfo(user, action = Action.START_ADD_PLACE, entityId = None)).mapTo[UserActivity]
      } yield {
        if (r1 > 1) logger.info(s"""save user info or user activity in database with error: more than one row updated""")
        addPlaceRouterActor ! SendStartAddPlaceToAddPlaceRouterActor(user)
      }).recover {
        case error: Throwable =>
          logger.info(s"""save user info or user activity in database with error: ${error.getMessage}""")
          self ! SendStartAddPlaceToUserActor(user)
      }

    case SaveUserWithUserInfo(user) =>

      val ref = sender()
      val newUser = User(
        id = Some(user.id),
        userName = user.username,
        firstName = user.first_name,
        lastName = user.last_name,
        isBot = user.is_bot,
        langCode = user.language_code,
        createdAt = DateTimeUtils.now,
        modifiedAt = DateTimeUtils.nowOpt
      )
      (postgresActor ? SaveUserInfo(newUser)).mapTo[Int].map { r =>
        elasticSearchActor ! SendIndexToElasticSearchActor(newUser.toJson, s"""/user_idx/user""")
        ref ! r
      }

    case SaveUserWithChatInfo(chat) =>

      val ref = sender()
      val user = User(
        id = Some(chat.id),
        userName = chat.username,
        firstName = chat.first_name,
        lastName = chat.last_name,
        createdAt = DateTimeUtils.now,
        modifiedAt = DateTimeUtils.nowOpt
      )
      (postgresActor ? SaveUserInfo(user)).mapTo[Int].map { r =>
        elasticSearchActor ! SendIndexToElasticSearchActor(user.toJson, s"""/user_idx/user""")
        ref ! r
      }

    case SaveUserActivityWithUserInfo(user, chat, action, entityId) =>

      val ref = sender()
      (postgresActor ? SaveUserActivity(UserActivity(
        userId = user.id,
        chatId = chat.id,
        action = action,
        entityId = entityId,
        createdAt = DateTimeUtils.now
      ))).mapTo[UserActivity].map { userActivity =>
        elasticSearchActor ! SendIndexToElasticSearchActor(userActivity.toJson, s"""/user_activity_idx/user_activity""")
        ref ! userActivity
      }

    case SaveUserActivityWithUserInfoWithoutChatInfo(user, action, entityId, metaData) =>

      val ref = sender()
      (postgresActor ? SaveUserActivity(UserActivity(
        userId = user.id,
        chatId = user.id,
        action = action,
        entityId = entityId,
        metaData = metaData,
        createdAt = DateTimeUtils.now
      ))).mapTo[UserActivity].map { userActivity =>
        elasticSearchActor ! SendIndexToElasticSearchActor(userActivity.toJson, s"""/user_activity_idx/user_activity""")
        ref ! userActivity
      }

    case SaveUserActivityWithChatInfo(chat, action, entityId) =>

      val ref = sender()
      (postgresActor ? SaveUserActivity(UserActivity(
        userId = chat.id,
        chatId = chat.id,
        action = action,
        entityId = entityId,
        createdAt = DateTimeUtils.now
      ))).mapTo[UserActivity].map { userActivity =>
        elasticSearchActor ! SendIndexToElasticSearchActor(userActivity.toJson, s"""/user_activity_idx/user_activity""")
        ref ! userActivity
      }

    case _ =>
      logger.info(s"""welcome to user actor""")

  }

  def convertSysToElasticTracking(userTracking: UserTracking): ElasticUserTracking = {
    ElasticUserTracking(
      id = userTracking.id,
      userId = userTracking.userId,
      text = s"""${userTracking.lat},${userTracking.lng}""",
      location = s"""${userTracking.lat},${userTracking.lng}""",
      createdAt = userTracking.createdAt,
      modifiedAt = userTracking.modifiedAt,
      disabled = userTracking.disabled,
      deleted = userTracking.deleted
    )
  }

}

object UserActor {

  import com.bot.models._

  object Action {

    val START_COMMAND = "start-command"
    val SET_GUILD = "set-guild"
    val SET_SUB_GUILD = "set-sub-guild"
    val SET_FILTER = "set-filter"
    val SET_DISTANCE = "set-distance"
    val SET_LOCATION = "set-location"
    val SET_PLACE_LIKE = "set-place-like"
    val SET_PLACE_DISLIKE = "set-place-dislike"
    val GO_TO_PLACE_POINT = "go-to-place-point"
    val SET_PLACE_POINT = "set-place-point"
    val SELECT_PLACE = "select-place"
    val GO_TO_PLACE_SET_REVIEW = "go-to-place-set-review"
    val SET_PLACE_REVIEW = "set-place-review"
    val GO_TO_PLACE_VIEW_REVIEWS = "go-to-place-view-reviews"
    val VIEW_NEXT_REVIEW = "view-next-review"
    val VIEW_PREVIEW_REVIEW = "view-preview-review"
    val VIEW_LAST_REVIEW = "view-last-review"
    val VIEW_FIRST_REVIEW = "view-first-review"
    val VIEW_NEXT_POINT = "view-next-point"
    val VIEW_PREVIEW_POINT = "view-preview-point"
    val VIEW_LAST_POINT = "view-last-point"
    val VIEW_FIRST_POINT = "view-first-point"
    val VIEW_NEXT_PLACE = "view-next-place"
    val VIEW_PREVIEW_PLACE = "view-preview-place"
    val VIEW_LAST_PLACE = "view-last-place"
    val VIEW_FIRST_PLACE = "view-first-place"
    val SET_REVIEW_LIKE = "set-review-like"
    val SET_REVIEW_DISLIKE = "set-review-dislike"
    val SET_FIND_PLACE = "set-find-place"
    val SET_SEARCH_PLACE = "set-search-place"
    val GET_INLINE_QUERY = "get-inline-query"
    val GET_CHOSEN_INLINE_RESULT = "get-chosen-inline-result"
    val GET_PLACE_INLINE_RESULT = "get-place-inline-result"
    val SET_ADD_PLACE = "set-add-place"
    val START_ADD_PLACE = "start-add-place"
    val SET_PLACE_CITY_ID = "set-place-city-id"
    val SET_PLACE_SUB_GUILD_ID = "set-place-sub-guild-id"
    val SET_PLACE_TITLE = "set-place-title"
    val SET_PLACE_ADDRESS = "set-place-address"
    val SET_PLACE_PHONE = "set-place-phone"
    val SET_PLACE_LOCATION = "set-place-location"
    val SET_PLACE_LINK = "set-place-link"
    val SET_EDIT_PLACE = "set-edit-place"
    val START_EDIT_PLACE = "start-edit-place"
    val EDIT_PLACE_CITY_ID = "edit-place-city-id"
    val EDIT_PLACE_SUB_GUILD_ID = "edit-place-sub-guild-id"
    val EDIT_PLACE_TITLE = "edit-place-title"
    val EDIT_PLACE_ADDRESS = "edit-place-address"
    val EDIT_PLACE_PHONE = "edit-place-phone"
    val EDIT_PLACE_LOCATION = "edit-place-location"
    val EDIT_PLACE_LINK = "edit-place-link"
    val EDITED_PLACE_CITY_ID = "edited-place-city-id"
    val EDITED_PLACE_SUB_GUILD_ID = "edited-place-sub-guild-id"
    val EDITED_PLACE_TITLE = "edited-place-title"
    val EDITED_PLACE_ADDRESS = "edited-place-address"
    val EDITED_PLACE_PHONE = "edited-place-phone"
    val EDITED_PLACE_LOCATION = "edited-place-location"
    val EDITED_PLACE_LINK = "edited-place-link"
  }

  val REVIEW_SAVED = "نظر شما ثبت شد."
  val REVIEW_NOT_SAVED = "نظر شما ثبت نشد."

  case class SaveUserWithUserInfo(user: json.bot.telegram.User)

  case class SaveUserWithChatInfo(user: json.bot.telegram.Chat)

  case class SaveUserActivityWithUserInfo(user: json.bot.telegram.User, chat: json.bot.telegram.Chat, action: String, entityId: Option[Long])

  case class SaveUserActivityWithUserInfoWithoutChatInfo(user: json.bot.telegram.User, action: String, entityId: Option[Long], metaData: Option[JsValue] = None)

  case class SaveUserActivityWithChatInfo(user: json.bot.telegram.Chat, action: String, entityId: Option[Long])

}

