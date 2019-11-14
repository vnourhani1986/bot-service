package com.bot.telegram

import akka.actor.{Actor, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.bot.formats.Formats._
import com.bot.models.emoji.Emoji._
import com.bot.models.json.bot.telegram._
import com.bot.models.json.callback.{Param, Query}
import com.bot.models.repo.filter.Filter
import com.bot.models.repo.guild.{Guild, SubGuild}
import com.bot.models.repo.place.Place
import com.bot.models.repo.review.UserQuestion
import com.bot.models.repo.user.{UserActivity, UserTracking}
import com.bot.services.telegram.TelegramBotService.Messages._
import com.bot.services.telegram.TelegramBotService._
import com.bot.telegram.CallbackDispatcherActor._
import com.bot.telegram.UserActor.Action
import com.bot.utils
import com.bot.utils.TelegramBotConfig
import com.typesafe.scalalogging.LazyLogging
import spray.json._

import scala.concurrent.ExecutionContext

class CallbackDispatcherActor(
                               implicit
                               system: ActorSystem,
                               ec: ExecutionContext,
                               timeout: Timeout
                             ) extends Actor with LazyLogging {

  self ! Start()

  override def receive(): Receive = {

    case Start() =>
      logger.info(s"""callback dispatcher actor is started""")

    case SendCallbackQueryToDispatcher(callbackQuery) =>

      val query = callbackQuery.data.map(_.parseJson.convertTo[Query]).get

      if (query.f == MainMenu.FROM_FIND_PLACE) {
        userActor ! SendFindPlaceToUserActor(callbackQuery.from, query.p.toJson)
      } else if (query.f == MainMenu.FROM_SEARCH_PLACE) {
        userActor ! SendSearchPlaceToUserActor(callbackQuery.from, query.p.toJson)
      } else if (query.f == MainMenu.FROM_ADD_PLACE) {
        userActor ! SendAddPlaceToUserActor(callbackQuery.from, query.p.toJson)
      } else if (query.f == MainMenu.FROM_EDIT_PLACE) {
        userActor ! SendEditPlaceToUserActor(callbackQuery.from, query.p.toJson)
      } else if (query.f == MainMenu.FROM_FIND_PLACE) {
        userActor ! SendSelectedSubGuildToUserActor(callbackQuery.from, query.p.sgId.get, query.p.toJson)
      } else if (query.f == GuildMenu.FROM_GUILD_MENU) {
        userActor ! SendSelectedGuildToUserActor(callbackQuery.from, query.p.gId.get, query.p.toJson)
      } else if (query.f == SubGuildMenu.FROM_SUB_GUILD_MENU) {
        userActor ! SendSelectedSubGuildToUserActor(callbackQuery.from, query.p.sgId.get, query.p.toJson)
      } else if (query.f == FilterMenu.FROM_FILTER_MENU) {
        userActor ! SendSelectedFilterToUserActor(callbackQuery.from, query.p.sgId.get, query.p.fId.get, query.p.toJson)
      } else if (query.f == DistanceFilterMenu.FROM_ONE_KILOMETER ||
        query.f == DistanceFilterMenu.FROM_TWO_KILOMETER ||
        query.f == DistanceFilterMenu.FROM_THREE_KILOMETER ||
        query.f == DistanceFilterMenu.FROM_FOUR_KILOMETER ||
        query.f == DistanceFilterMenu.FROM_FIVE_KILOMETER ||
        query.f == DistanceFilterMenu.FROM_MORE) {
        userActor ! SendSelectedDistanceFilterToUserActor(callbackQuery.from, query.p.sgId.get, query.p.fId.get, query.p.dst.get, query.p.toJson)
      } else if (query.f == PlaceMenu.FROM_PLACE_LIST) {
        userActor ! SendSelectedPlaceToUserActor(getCallbackMessageId(callbackQuery).getOrElse(0), callbackQuery.from, query.p.pId.get, query.p.toJson, Action.SELECT_PLACE, FromButton.PLACE_LIST)
      } else if (query.f == PlaceMenu.FROM_SHOW_FIRST_PLACE) {
        userActor ! SendOtherPagePlaceListToUserActor(getCallbackMessageId(callbackQuery).getOrElse(0), callbackQuery.from, query.p.toJson, 1, last = false, Action.VIEW_FIRST_PLACE, FromButton.PLACE_LIST)
      } else if (query.f == PlaceMenu.FROM_SHOW_PREVIEW_PLACE) {
        userActor ! SendOtherPagePlaceListToUserActor(getCallbackMessageId(callbackQuery).getOrElse(0), callbackQuery.from, query.p.toJson, query.p.pg.get - 1, last = false, Action.VIEW_PREVIEW_PLACE, FromButton.PLACE_LIST)
      } else if (query.f == PlaceMenu.FROM_SHOW_NEXT_PLACE) {
        userActor ! SendOtherPagePlaceListToUserActor(getCallbackMessageId(callbackQuery).getOrElse(0), callbackQuery.from, query.p.toJson, query.p.pg.get + 1, last = false, Action.VIEW_NEXT_PLACE, FromButton.PLACE_LIST)
      } else if (query.f == PlaceMenu.FROM_SHOW_LAST_PLACE) {
        userActor ! SendOtherPagePlaceListToUserActor(getCallbackMessageId(callbackQuery).getOrElse(0), callbackQuery.from, query.p.toJson, query.p.pg.get, last = true, Action.VIEW_LAST_PLACE, FromButton.PLACE_LIST)
      } else if (query.f == PlaceMenu.FROM_PLACE_LIKE) {
        userActor ! SendPlaceLikeToUserActor(callbackQuery.from, query.p.pId.get, like = true, query.p.toJson)
      } else if (query.f == PlaceMenu.FROM_PLACE_DISLIKE) {
        userActor ! SendPlaceLikeToUserActor(callbackQuery.from, query.p.pId.get, like = false, query.p.toJson)
      } else if (query.f == PlaceMenu.FROM_PLACE_SET_POINT) {
        userActor ! SendSelectedPointToUserActor(getCallbackMessageId(callbackQuery).getOrElse(0), callbackQuery.from, query.p.pId.get, query.p.toJson, 1, last = false, Action.SET_PLACE_POINT, FromButton.PLACE)
      } else if (query.f == PointMenu.FROM_BAD || query.f == PointMenu.FROM_NORMAL || query.f == PointMenu.FROM_EXCELLENT) {
        userActor ! SendSelectedPointItemToUserActor(callbackQuery.from, query.p.pId.get, query.p.qId.get, query.p.rate.get, query.p.toJson)
      } else if (query.f == PointMenu.FROM_SHOW_FIRST_POINT) {
        userActor ! SendSelectedPointToUserActor(getCallbackMessageId(callbackQuery).getOrElse(0), callbackQuery.from, query.p.pId.get, query.p.toJson, 1, last = false, Action.VIEW_FIRST_POINT, FromButton.POINT)
      } else if (query.f == PointMenu.FROM_SHOW_PREVIEW_POINT) {
        userActor ! SendSelectedPointToUserActor(getCallbackMessageId(callbackQuery).getOrElse(0), callbackQuery.from, query.p.pId.get, query.p.toJson, if (query.p.pg.get <= 1) query.p.pg.get else query.p.pg.get - 1, last = false, Action.VIEW_PREVIEW_POINT, FromButton.POINT)
      } else if (query.f == PointMenu.FROM_SHOW_NEXT_POINT) {
        userActor ! SendSelectedPointToUserActor(getCallbackMessageId(callbackQuery).getOrElse(0), callbackQuery.from, query.p.pId.get, query.p.toJson, if (query.p.pg.get >= 3) query.p.pg.get else query.p.pg.get + 1, last = false, Action.VIEW_NEXT_POINT, FromButton.POINT)
      } else if (query.f == PointMenu.FROM_SHOW_LAST_POINT) {
        userActor ! SendSelectedPointToUserActor(getCallbackMessageId(callbackQuery).getOrElse(0), callbackQuery.from, query.p.pId.get, query.p.toJson, query.p.pg.get, last = true, Action.VIEW_LAST_POINT, FromButton.POINT)
      } else if (query.f == PlaceMenu.FROM_PLACE_SHOW_COMMENT) {
        userActor ! SendShowReviewToUserActor(getCallbackMessageId(callbackQuery).getOrElse(0), callbackQuery.from, query.p.pId.get, 1, query.p.toJson, last = false, Action.VIEW_FIRST_REVIEW, FromButton.PLACE)
      } else if (query.f == CommentMenu.FROM_SHOW_FIRST_COMMENT) {
        userActor ! SendShowReviewToUserActor(getCallbackMessageId(callbackQuery).getOrElse(0), callbackQuery.from, query.p.pId.get, 1, query.p.toJson, last = false, Action.VIEW_FIRST_REVIEW, FromButton.REVIEW)
      } else if (query.f == CommentMenu.FROM_SHOW_PREVIEW_COMMENT) {
        userActor ! SendShowReviewToUserActor(getCallbackMessageId(callbackQuery).getOrElse(0), callbackQuery.from, query.p.pId.get, query.p.pg.get - 1, query.p.toJson, last = false, Action.VIEW_PREVIEW_REVIEW, FromButton.REVIEW)
      } else if (query.f == CommentMenu.FROM_SHOW_NEXT_COMMENT) {
        userActor ! SendShowReviewToUserActor(getCallbackMessageId(callbackQuery).getOrElse(0), callbackQuery.from, query.p.pId.get, query.p.pg.get + 1, query.p.toJson, last = false, Action.VIEW_NEXT_REVIEW, FromButton.REVIEW)
      } else if (query.f == CommentMenu.FROM_SHOW_LAST_COMMENT) {
        userActor ! SendShowReviewToUserActor(getCallbackMessageId(callbackQuery).getOrElse(0), callbackQuery.from, query.p.pId.get, query.p.pg.get, query.p.toJson, last = true, action = Action.VIEW_LAST_REVIEW, FromButton.REVIEW)
      } else if (query.f == CommentMenu.FROM_LIKE_COMMENT) {
        userActor ! SendReviewLikeToUserActor(callbackQuery.from, query.p.rId.get, like = true, query.p.toJson)
      } else if (query.f == CommentMenu.FROM_DISLIKE_COMMENT) {
        userActor ! SendReviewLikeToUserActor(callbackQuery.from, query.p.rId.get, like = false, query.p.toJson)
      } else if (query.f == PlaceMenu.FROM_PLACE_SET_COMMENT) {
        userActor ! SendSetReviewToUserActor(callbackQuery.from, query.p.pId.get, query.p.toJson)
      } else if (query.f == PlaceMenu.FROM_ADD_PLACE_WELCOME) {
        userActor ! SendStartAddPlaceToUserActor(callbackQuery.from)
      } else if (query.f == MainMenu.FROM_BACK_TO_MAIN_MENU) {
        self ! BackToMainMenu(callbackQuery)
      }

      senderActor ! AnswerCallbackQuery(callbackQuery.id)

    case SelectGetLocationButton(callbackQuery, subGuildId) =>

      val keyboardButton = locationButtons.map(_.map(location => KeyboardButton(
        text = location._1,
        request_location = Some(true),
        request_contact = Some(false)
      )))

      senderActor ! SendMessage1(callbackQuery.from.id, "s", reply_markup = Some(ReplyKeyboardMarkup(keyboardButton)))

    case CreateMainMenu(chatId) =>

      val inlineKeyboardMarkup = mainMenuButtons.map(_.map(data => InlineKeyboardButton(
        text = data._1,
        callback_data = Some(Query(
          f = data._2,
          p = Param(
          )).toJson.compactPrint)
      )))

      senderActor ! SendPhoto(chat_id = chatId,
        photo = TelegramBotConfig.startCommandPhoto,
        caption = Some(MainMenu.MAIN_MENU_TITLE),
        parse_mode = Some("HTML"),
        reply_markup = Some(InlineKeyboardMarkup(inlineKeyboardMarkup)))

    case CreateBackToMainMenu(chatId, photo, caption) =>

      val inlineKeyboardMarkup = backToMainMenuButtons.map(_.map(data => InlineKeyboardButton(
        text = data._1,
        callback_data = Some(Query(
          f = data._2,
          p = Param(
          )).toJson.compactPrint)
      )))

      senderActor ! SendPhoto(chat_id = chatId,
        photo = photo,
        caption = Some(caption),
        parse_mode = Some("HTML"),
        reply_markup = Some(InlineKeyboardMarkup(inlineKeyboardMarkup)))

    case BackToMainMenu(callbackQuery) =>

      commandActor ! StartCommand(callbackQuery.from.id)

    case SendFindPlaceToUserActor(user, metaData) =>

      for {

        guilds <- (postgresActor ? GetGuilds()).mapTo[Seq[Guild]]

      } yield {

        val buttons1 = guilds.filter(_.title.isDefined).filter(_.title.get.length < 18).map { guild =>
          InlineKeyboardButton(
            text = guild.title.get,
            callback_data =
              Some(Query(
                f = GuildMenu.FROM_GUILD_MENU,
                p = Param(
                  gId = guild.id
                )
              ).toJson.compactPrint)
          )
        }.toList

        val buttons2 = guilds.filter(_.title.isDefined).filter(_.title.get.length >= 18).map { guild =>
          InlineKeyboardButton(
            text = guild.title.get,
            callback_data =
              Some(Query(
                f = GuildMenu.FROM_GUILD_MENU,
                p = Param(
                  gId = guild.id
                )
              ).toJson.compactPrint)
          )
        }.toList

        val (bs1, bs2) = if (buttons1.length % 2 == 0) {
          (buttons1, buttons2)
        } else {
          (buttons1.drop(1), buttons2 ++ List(buttons1.head))
        }

        val inlineKeyboardMarkup1 = utils.splitColumns2(bs1)
        val inlineKeyboardMarkup2 = utils.splitColumns1(bs2)
        val inlineKeyboardMarkup = inlineKeyboardMarkup1 ++ inlineKeyboardMarkup2

        senderActor ! SendPhoto(chat_id = user.id, photo = TelegramBotConfig.setGuildPhoto, caption = Some(GuildMenu.GUILD_MENU_TITLE), reply_markup = Some(InlineKeyboardMarkup(inlineKeyboardMarkup)))

      }

    case SendSearchPlaceToUserActor(user, metaData) =>

      self ! CreateBackToMainMenu(
        user.id,
        TelegramBotConfig.inlineSearchPhoto,
        s"""${SearchPlaceContents.GO_TO_VIEW_COMMANDS_1}
           |@${TelegramBotConfig.name} ${SearchPlaceContents.GO_TO_VIEW_COMMANDS_2}
           |${SearchPlaceContents.GO_TO_VIEW_COMMANDS_3}
           |""".stripMargin
      )

    case SendSelectedGuildToUserActor(user, guildId, metaData) =>

      for {
        subGuilds <- (postgresActor ? GetSubGuilds(guildId)).mapTo[Seq[SubGuild]]
      } yield {
        val buttons1 = subGuilds.filter(_.title.isDefined).filter(_.title.get.length < 18).map { subGuild =>
          InlineKeyboardButton(
            text = subGuild.title.get,
            callback_data =
              Some(Query(
                f = SubGuildMenu.FROM_SUB_GUILD_MENU,
                p = Param(
                  sgId = subGuild.id
                )
              ).toJson.compactPrint)
          )
        }.toList

        val buttons2 = subGuilds.filter(_.title.isDefined).filter(_.title.get.length >= 18).map { subGuild =>
          InlineKeyboardButton(
            text = subGuild.title.get,
            callback_data =
              Some(Query(
                f = SubGuildMenu.FROM_SUB_GUILD_MENU,
                p = Param(
                  sgId = subGuild.id
                )
              ).toJson.compactPrint)
          )
        }.toList

        val (bs1, bs2) = if (buttons1.length % 2 == 0) {
          (buttons1, buttons2)
        } else {
          (buttons1.drop(1), buttons2 ++ List(buttons1.head))
        }

        val inlineKeyboardMarkup1 = utils.splitColumns2(bs1)
        val inlineKeyboardMarkup2 = utils.splitColumns1(bs2)
        val inlineKeyboardMarkup = inlineKeyboardMarkup1 ++ inlineKeyboardMarkup2

        senderActor ! SendPhoto(chat_id = user.id, photo = TelegramBotConfig.setSubGuildPhoto, caption = Some(SubGuildMenu.SUB_GUILD_MENU_TITLE), reply_markup = Some(InlineKeyboardMarkup(inlineKeyboardMarkup)))

      }

    case SendAddPlaceToUserActor(user, metaData) =>

      addPlaceRouterActor ! SendAddPlaceToAddPlaceRouterActor(user)

    case SendEditPlaceToUserActor(user, metaData) =>

      editPlaceActor ! SendEditPlaceToEditPlaceActor(user)

    case SendSelectedSubGuildToUserActor(user, subGuildId, metaData) =>

      for {
        filters <- (postgresActor ? GetFilters()).mapTo[Seq[Filter]]
      } yield {
        val buttons = filters.map { filter =>
          InlineKeyboardButton(
            text = filter.title,
            callback_data =
              Some(Query(
                f = FilterMenu.FROM_FILTER_MENU,
                p = Param(
                  sgId = Some(subGuildId),
                  fId = filter.id
                )
              ).toJson.compactPrint)
          )
        }.toList

        val inlineKeyboardMarkup = utils.splitColumns2(buttons)

        senderActor ! SendPhoto(chat_id = user.id,
          photo = TelegramBotConfig.setFilterPhoto,
          caption = Some(FilterMenu.FILTER_MENU_TITLE),
          reply_markup = Some(InlineKeyboardMarkup(inlineKeyboardMarkup)))

      }

    case SendSelectedFilterToUserActor(user, subGuildId, filterId, metaData) =>

      for {
        filters <- (postgresActor ? GetFilters()).mapTo[Seq[Filter]]
      } yield {
        if (filterId == filters.find(_.title == "near").flatMap(_.id).getOrElse(1l)) {
          val inlineKeyboardMarkup = distanceButtons.map(_.map(dFilter => InlineKeyboardButton(
            text = dFilter._1._1,
            callback_data = Some(Query(
              f = dFilter._1._2,
              p = Param(
                sgId = Some(subGuildId),
                fId = Some(filterId),
                dst = Some(dFilter._2)
              )).toJson.compactPrint)
          )))

          senderActor ! SendPhoto(chat_id = user.id, photo = TelegramBotConfig.setDistancePhoto, caption = Some(DistanceFilterMenu.DISTANCE_FILTER_MENU_TITLE), reply_markup = Some(InlineKeyboardMarkup(inlineKeyboardMarkup)))
        } else {

          val keyboardButton = locationButtons.map(_.map(location => KeyboardButton(
            text = location._1,
            request_location = Some(true),
            request_contact = Some(false)
          )))

          senderActor ! SendPhoto1(chat_id = user.id,
            photo = TelegramBotConfig.setLocationPhoto,
            caption = Some(LocationMenu.SET_LOCATION_TITLE),
            parse_mode = Some("HTML"),
            reply_markup = Some(ReplyKeyboardMarkup(keyboardButton)))

        }
      }

    case SendSelectedDistanceFilterToUserActor(user, subGuildId, filterId, distance, metaData) =>

      val keyboardButton = locationButtons.map(_.map(location => KeyboardButton(
        text = location._1,
        request_location = Some(true),
        request_contact = Some(false)
      )))

      senderActor ! SendPhoto1(chat_id = user.id,
        photo = TelegramBotConfig.setLocationPhoto,
        caption = Some(LocationMenu.SET_LOCATION_TITLE),
        parse_mode = Some("HTML"),
        reply_markup = Some(ReplyKeyboardMarkup(keyboardButton)))

    case SendCreateVenueToCallbackActor(messageId, chatId, places, from) =>

      places.foreach { p =>
        val (place, likes, dislikes) = p
        val inlineKeyboardMarkup = placeButtons.map(_.map(pButton => InlineKeyboardButton(
          text = pButton._1,
          callback_data = Some(Query(
            f = pButton._2,
            p = Param(
              pId = place.id
            )).toJson.compactPrint)
        )))

        senderActor ! SendVenue(
          chatId,
          place.lat.getOrElse(0d).toFloat,
          place.lng.getOrElse(0d).toFloat,
          place.title.getOrElse(""),
          s"""${place.phone.getOrElse(PlaceMenu.FROM_SHOW_PLACE_NO_PHONE_TITLE)} ${PlaceMenu.FROM_SHOW_PLACE_PHONE_TITLE} $THUMBS_DOWN $dislikes $THUMBS_UP $likes
             |${place.address.getOrElse("")}""".stripMargin,
          reply_markup = Some(InlineKeyboardMarkup(inlineKeyboardMarkup))
        )

      }

    case SendPlaceListToCallbackActor(messageId, chatId, places, page, totalPage, last, from) =>

      val placeList = places.filter(_.title.isDefined).map { place =>
        InlineKeyboardButton(
          text = place.title.get,
          callback_data = Some(Query(
            f = PlaceMenu.FROM_PLACE_LIST,
            p = Param(
              mId = Some(messageId),
              pId = place.id
            )).toJson.compactPrint)
        )
      }.toList

      val inlineKeyboardMarkup2 = previewPlaceButtons.map(_.map(previewPlace => InlineKeyboardButton(
        text = if (previewPlace._2 == PlaceMenu.FROM_SHOW_NUMBER_OF_PLACE) s"""${if (last) totalPage else page}/$totalPage""" else previewPlace._1,
        callback_data = Some(Query(
          f = previewPlace._2,
          p = Param(
            mId = Some(messageId),
            pg = Some(page)
          )).toJson.compactPrint)
      )))

      val inlineKeyboardMarkup1 = utils.splitColumns1(placeList)
      val inlineKeyboardMarkup = inlineKeyboardMarkup1 ++ inlineKeyboardMarkup2

      if (from == FromButton.PLACE_LIST) {
        senderActor ! EditMessageCaption(chat_id = chatId,
          message_id = Some(messageId),
          caption = Some(PlaceMenu.FROM_PLACE_LIST_TITLE),
          reply_markup = Some(InlineKeyboardMarkup(inlineKeyboardMarkup))
        )
      } else {
        senderActor ! SendPhoto(chat_id = chatId,
          photo = TelegramBotConfig.placeListPhoto,
          caption = Some(PlaceMenu.FROM_PLACE_LIST_TITLE),
          reply_markup = Some(InlineKeyboardMarkup(inlineKeyboardMarkup))
        )
      }

    case SendOtherPagePlaceListToUserActor(messageId, user, metaData, page, last, action, from) =>

      (for {
        lastActivity <- (postgresActor ? GetLastActivityForSpecificAction(user.id, Action.SET_DISTANCE)).mapTo[Option[UserActivity]]
        distanceMetadata = lastActivity.flatMap(_.metaData).map(_.convertTo[Param])
        filterId = distanceMetadata.flatMap(_.fId).getOrElse(1l)
        subGuildId = distanceMetadata.flatMap(_.sgId).getOrElse(0l)
        distance = distanceMetadata.flatMap(_.dst).getOrElse(10d)
        filters <- (postgresActor ? GetFilters()).mapTo[Seq[Filter]]
        userTracking <- (postgresActor ? GetLastLocation(user.id)).mapTo[Option[UserTracking]]
        location = userTracking.map(x => Location(latitude = x.lat.toFloat, longitude = x.lng.toFloat)).getOrElse(Location(0f, 0f))
      } yield {
        if (filters.filter(_.id.get == filterId).map(_.data).head == "near") {
          findPlaceActor ! GetNearByDistanceCalc(messageId, user.id, List(subGuildId), location, distance, page, 10, last = last, from = from)
        } else if (filters.filter(_.id == filterId).map(_.data).head == "pop") {
          findPlaceActor ! GetPopPlaces(messageId, user.id, List(subGuildId), location, 10d, page, 10, last = last, from = from)
        }
      }).recover {
        case error: Throwable =>
          logger.info(s"""find preview request place info in database with error: ${error.getMessage}""")
      }

    case SendSelectedPlaceToUserActor(messageId, user, placeId, metaData, action, from) =>

      findPlaceActor ! GetPlace(messageId, user.id, placeId, from)

    case SendSelectedPointToUserActor(messageId, user, placeId, metaData, page, last, action, from) =>

      (for {
        places <- (postgresActor ? GetPlaceToPostgresActor(placeId)).mapTo[Seq[Place]]
        questions <- (postgresActor ? GetQuestions(places.flatMap(_.childGuildIds).headOption.getOrElse(0l), page - 1, 1)).mapTo[Seq[UserQuestion]]
      } yield {
        val placeTitle = places.flatMap(_.title).headOption.getOrElse("")
        questions.foreach(q => callbackDispatcherActor ! CreatePointQuestion(messageId, user, placeId, placeTitle, q, page, 3, last = last, from))
      }).recover {
        case error: Throwable =>
          logger.info(s"""get questions from database with error: ${error.getMessage}""")
      }

    case CreatePointQuestion(messageId, user, placeId, placeTitle, question, page, totalPage, last, from) =>

      val inlineKeyboardMarkup = pointsButtons.map(_.map(point => InlineKeyboardButton(
        text = if (point._2 == PointMenu.FROM_SHOW_NUMBER_OF_POINT) s"""${if (last) totalPage else page}/$totalPage""" else point._1,
        callback_data =
          Some(Query(
            f = point._2,
            p = Param(
              pId = Some(placeId),
              qId = question.id,
              pg = Some(page),
              rate = if (point._2 == PointMenu.FROM_BAD) {
                Some(1)
              } else if (point._2 == PointMenu.FROM_NORMAL) {
                Some(2)
              } else if (point._2 == PointMenu.FROM_EXCELLENT) {
                Some(3)
              } else {
                None
              }
            )
          ).toJson.compactPrint)
      )))

      if (from == FromButton.PLACE) {
        senderActor ! SendPhoto(chat_id = user.id,
          photo = TelegramBotConfig.setPointPhoto,
          caption = Some(
            s"""<b>$placeTitle</b>
               |${question.title}""".stripMargin),
          parse_mode = Some("HTML"),
          reply_markup = Some(InlineKeyboardMarkup(inlineKeyboardMarkup)))
      } else {
        senderActor ! EditMessageCaption(chat_id = user.id,
          message_id = Some(messageId),
          caption = Some(
            s"""<b>$placeTitle</b>
               |${question.title}""".stripMargin),
          parse_mode = Some("HTML"),
          reply_markup = Some(InlineKeyboardMarkup(inlineKeyboardMarkup)))
      }


    case SendShowReviewToUserActor(messageId, user, placeId, page, metaData, last, action, from) =>

      for {
        (reviewId, placeTitle, content) <- (postgresActor ? GetReview(page, placeId, last)).mapTo[(Option[Long], Option[String], Option[String])]
        totalReviews <- (postgresActor ? GetTotalReviews(placeId)).mapTo[Int]
        (likes, dislikes) <- (postgresActor ? GetReviewTotalLikes(reviewId.getOrElse(0l))).mapTo[(Int, Int)]
      } yield {
        if (totalReviews == 0) {
          callbackDispatcherActor ! CreateBackToMainMenu(user.id,
            TelegramBotConfig.failPhoto,
            s"""<b>${placeTitle.getOrElse("")}</b>
               |${CommentMenu.FROM_SHOW_NO_CONTENT}""".stripMargin)
        } else {
          content.foreach(comment => callbackDispatcherActor ! SelectShowReviewButton(messageId, user, reviewId, placeId, placeTitle.getOrElse(""), comment, likes, dislikes, if (last) totalReviews else page, totalReviews, last, from))
        }
      }


    case SelectShowReviewButton(messageId, user, reviewId, placeId, placeTitle, comment, likes, dislikes, page, totalPage, last, from) =>

      val inlineKeyboardMarkup = showCommentButtons.map(_.map(filter => InlineKeyboardButton(
        text = if (filter._2 == CommentMenu.FROM_SHOW_NUMBER_OF_COMMENT) s"""${if (last) totalPage else page}/$totalPage""" else filter._1,
        callback_data = Some(Query(
          f = filter._2,
          p = Param(
            pId = Some(placeId),
            rId = reviewId,
            pg = Some(page)
          )).toJson.compactPrint)
      )))

      if (from == FromButton.PLACE) {
        senderActor ! SendPhoto(chat_id = user.id,
          photo = TelegramBotConfig.showReviewPhoto,
          caption = Some(
            s"""<b>$placeTitle</b>
               |$comment
               |$likes $THUMBS_UP $dislikes $THUMBS_DOWN""".stripMargin),
          parse_mode = Some("HTML"),
          reply_markup = Some(InlineKeyboardMarkup(inlineKeyboardMarkup)))
      } else {
        senderActor ! EditMessageCaption(chat_id = user.id,
          message_id = Some(messageId),
          caption = Some(
            s"""<b>$placeTitle</b>
               |$comment
               |$likes $THUMBS_UP $dislikes $THUMBS_DOWN""".stripMargin),
          parse_mode = Some("HTML"),
          reply_markup = Some(InlineKeyboardMarkup(inlineKeyboardMarkup)))
      }

    case SendSetReviewToUserActor(user, placeId, metaData) =>

      (postgresActor ? GetPlaces(Seq(placeId))).mapTo[Seq[Place]].map { places =>
        senderActor ! SendPhoto(chat_id = user.id,
          photo = TelegramBotConfig.setReviewPhoto,
          caption = Some(
            s"""<b>${places.flatMap(_.title).headOption.getOrElse("")}</b>
               |${CommentMenu.FROM_SET_TITLE}""".stripMargin),
          parse_mode = Some("HTML"))
      }


    case SendAddPlaceWelcomeToCallbackDispatcher(user, caption, keyboardTitle) =>

      val buttons = List(InlineKeyboardButton(
        text = keyboardTitle,
        callback_data =
          Some(Query(
            f = PlaceMenu.FROM_ADD_PLACE_WELCOME,
            p = Param(

            )
          ).toJson.compactPrint)
      ))

      val inlineKeyboardMarkup = utils.splitColumns1(buttons)

      senderActor ! SendPhoto(chat_id = user.id, photo = TelegramBotConfig.addPlacePhoto, caption = Some(caption), reply_markup = Some(InlineKeyboardMarkup(inlineKeyboardMarkup)))

    case _ =>
      logger.info(s"""welcome to callback dispatcher actor""")


  }

}

object CallbackDispatcherActor {

  object GuildMenu {
    val FROM_GUILD_MENU = "fgm"
    val GUILD_MENU_TITLE = "لطفا یکی از دسته بندی های زیر را انتخاب نمایید"
  }

  object SubGuildMenu {
    val FROM_SUB_GUILD_MENU = "fsgm"
    val SUB_GUILD_MENU_TITLE = "لطفا یکی از اصناف زیر را انتخاب نمایید"
  }

  object FilterMenu {
    val FROM_FILTER_MENU = "ffm"
    val FILTER_MENU_TITLE = "لطفا اولویت خود جهت نمایش مکان های را تعیین نمایید"
    val FROM_NEAREST = "near"
    val FROM_POPULARITY = "pop"
  }

  object DistanceFilterMenu {
    val FROM_DISTANCE_FILTER_MENU = "fdfm"
    val DISTANCE_FILTER_MENU_TITLE = "لطفا حداکثر فاصله مکان های مورد نظر از مکان جاری خود را تعیین نمایید"
    val FROM_ONE_KILOMETER = "1k"
    val FROM_ONE_KILOMETER_TITLE = "یک کیلومتر"
    val FROM_TWO_KILOMETER = "2k"
    val FROM_TWO_KILOMETER_TITLE = "دو کیلومتر"
    val FROM_THREE_KILOMETER = "3k"
    val FROM_THREE_KILOMETER_TITLE = "سه کیلومتر"
    val FROM_FOUR_KILOMETER = "4k"
    val FROM_FOUR_KILOMETER_TITLE = "چهار کیلومتر"
    val FROM_FIVE_KILOMETER = "5k"
    val FROM_FIVE_KILOMETER_TITLE = "پنج کیلومتر"
    val FROM_MORE = "m"
    val FROM_MORE_TITLE = "بیشتر"
  }

  object LocationMenu {
    val GET_LOCATION_TITLE: String = "لوکیشن"
    val FROM_GET_LOCATION = "fgl"
    val SET_LOCATION_TITLE = "لطفا لوکیشن خود را وارد نمایید"
    val SET_LOCATION_TITLE_1 = "در نهایت ترینت نیاز دارد لوکیشن جاری شما را جهت یافتن نتایج مطلوب بداند. شما می توانید لوکیشن فعلی خود و یا هر لوکیشن دیگری را انتخاب تا با توجه به آن بهترین گزینه ها نمایش داده شوند. دو روش جهت دریافت لوکیشن وجود دارد."
    val SET_LOCATION_TITLE_2 = "۱) انتخاب لوکیشن از طریق دکمه پایین صفحه: این روش تنها قابلیت ارسال لوکیشن فعلی شما را به سرویس ترینت دارد."
    val SET_LOCATION_TITLE_3 = "۲) استفاده از روش معمول در تلگرام: جهت آشنایی با چگونگی استفاده از این روش در گوشی های اندروید از لینک زیر"
    val SET_LOCATION_TITLE_4 = "https://www.youtube.com/watch?v=6lQVkQGm4aU"
    val SET_LOCATION_TITLE_5 = "و در گوشی های آی او اس از لینک زیر کمک بگیرید."
    val SET_LOCATION_TITLE_6 = "https://www.youtube.com/watch?v=e270rWBCYoc"
    val SET_LOCATION_TITLE_7 = "حال یکی از دو روش بالا را انتخاب نمایید تا مکان های مربوطه نمایش داده شوند."
  }

  object PlaceMenu {
    val FROM_PLACE = "fp"
    val FROM_PLACE_LIKE_TITLE: String = THUMBS_UP
    val FROM_PLACE_LIKE = "fplb"
    val FROM_PLACE_DIS_LIKE_TITLE: String = THUMBS_DOWN
    val FROM_PLACE_DISLIKE = "fpdlb"
    val FROM_PLACE_SET_POINT_TITLE: String = STAR
    val FROM_PLACE_SET_POINT = "sp"
    val FROM_PLACE_SET_COMMENT_TITLE = "ثبت نظر"
    val FROM_PLACE_SET_COMMENT = "comment"
    val FROM_PLACE_SHOW_COMMENT_TITLE = "مشاهده نظرات"
    val FROM_PLACE_SHOW_COMMENT = "sc"
    val FROM_SHOW_PLACE_PHONE_TITLE = "شماره تماس"
    val FROM_SHOW_PLACE_NO_PHONE_TITLE = "بدون"
    val FROM_SHOW_LAST_PLACE_TITLE = ">>"
    val FROM_SHOW_LAST_PLACE = "l-pl"
    val FROM_SHOW_FIRST_PLACE_TITLE = "<<"
    val FROM_SHOW_FIRST_PLACE = "f-pl"
    val FROM_SHOW_NEXT_PLACE_TITLE = ">"
    val FROM_SHOW_NEXT_PLACE = "n-pl"
    val FROM_SHOW_PREVIEW_PLACE_TITLE = "<"
    val FROM_SHOW_PREVIEW_PLACE = "p-pl"
    val FROM_SHOW_NUMBER_OF_PLACE_TITLE = "100"
    val FROM_SHOW_NUMBER_OF_PLACE = "n-of-pl"
    val FROM_ADD_PLACE_WELCOME_TITLE = ""
    val FROM_ADD_PLACE_WELCOME = "apw"
    val FROM_PLACE_LIST_TITLE = "لیست مکان های پیشنهادی به قرار زیر می باشند"
    val FROM_PLACE_LIST = "plt"
  }

  object PointMenu {
    val FROM_POINT_MENU_TITLE = "لیست سوالات"
    val FROM_POINT_MENU = "fpmd"
    val FROM_POINT = "fpd"
    val FROM_EXCELLENT = "excellent"
    val FROM_EXCELLENT_TITLE: String = "عالی" + SMILING_FACE_WITH_HEART_EYES
    val FROM_NORMAL = "normal"
    val FROM_NORMAL_TITLE: String = "معمولی" + NEUTRAL_FACE
    val FROM_BAD = "bad"
    val FROM_BAD_TITLE: String = "بد" + FROWNING_FACE
    val FROM_SHOW_LAST_POINT_TITLE = ">>"
    val FROM_SHOW_LAST_POINT = "l-p"
    val FROM_SHOW_FIRST_POINT_TITLE = "<<"
    val FROM_SHOW_FIRST_POINT = "f-p"
    val FROM_SHOW_NEXT_POINT_TITLE = ">"
    val FROM_SHOW_NEXT_POINT = "n-p"
    val FROM_SHOW_PREVIEW_POINT_TITLE = "<"
    val FROM_SHOW_PREVIEW_POINT = "p-p"
    val FROM_SHOW_NUMBER_OF_POINT_TITLE = "100"
    val FROM_SHOW_NUMBER_OF_POINT = "n-of-p"
  }

  object CommentMenu {
    val FROM_SHOW_COMMENT_MENU = "fsm"
    val FROM_SET_COMMENT_MENU = "fsem"
    val FROM_SET_TITLE = "نظر خود را با مشتری های آینده این کسب و کار به اشتراک بگذارید. مثلا برای رستوران می توانید در مورد کیفیت غذاها، قیمت، فضای رستوران، برخورد کارکنان و … بنویسید."
    val FROM_SHOW_NO_CONTENT = "هیچ نظری در ارتباط با مکان مورد نظر یافت نشد. با ثبت نظر خود اولین نفری باشید که نظر خود را در ارتباط با این مکان ثبت می کنید."
    val FROM_SET_COMMENT_TITLE = "ثبت نظر"
    val FROM_SET_COMMENT = "s-c"
    val FROM_LIKE_COMMENT_TITLE: String = THUMBS_UP
    val FROM_LIKE_COMMENT: String = "slct"
    val FROM_DISLIKE_COMMENT_TITLE: String = THUMBS_DOWN
    val FROM_DISLIKE_COMMENT: String = "sdlct"
    val FROM_SHOW_LAST_COMMENT_TITLE = ">>"
    val FROM_SHOW_LAST_COMMENT = "l-c"
    val FROM_SHOW_FIRST_COMMENT_TITLE = "<<"
    val FROM_SHOW_FIRST_COMMENT = "f-c"
    val FROM_SHOW_NEXT_COMMENT_TITLE = ">"
    val FROM_SHOW_NEXT_COMMENT = "n-c"
    val FROM_SHOW_PREVIEW_COMMENT_TITLE = "<"
    val FROM_SHOW_PREVIEW_COMMENT = "p-c"
    val FROM_SHOW_NUMBER_OF_COMMENT_TITLE = "100"
    val FROM_SHOW_NUMBER_OF_COMMENT = "n-of-c"
  }

  object MainMenu {
    val MAIN_MENU_TITLE = "به سرویس مکان یابی ترینت خوش آمدید"
    val FROM_FIND_PLACE = "ffp"
    val FROM_SEARCH_PLACE = "fsp"
    val FROM_ADD_PLACE = "fap"
    val FROM_EDIT_PLACE = "fep"
    val FROM_HELP = "fh"
    val FROM_BACK_TO_MAIN_MENU = "fbmm"
    val FIND_PLACE_TITLE = "جستجوی مکان های سراسر کشور"
    val SEARCH_PLACE_TITLE = "جستجوی متنی مکان های سراسر کشور"
    val ADD_PLACE_TITLE = "افزودن مکان جدید"
    val EDIT_PLACE_TITLE = "ویرایش مکان ثبت شده"
    val HELP_TITLE = "دستورالعمل استفاده از بات"
    val BACK_TO_MAIN_MENU_TITLE = "بازگشت به منوی اصلی"
  }

  object SearchPlaceContents {
    val GO_TO_VIEW_COMMANDS_1 = "استفاده از موتور جستجوی ترینت این امکان را فراهم می کند تا مکان های مورد نظر خود را از طریق وارد کردن نام آنها یا کلمات کلیدی مرتبط پیدا کنید. به عنوان مثال جهت یافتن قنادی لادن می توانید دستور زیر را در قسمت ورودی متن وارد کنید."
    val GO_TO_VIEW_COMMANDS_2 = "قنادی لادن"
    val GO_TO_VIEW_COMMANDS_3 = "استفاده از این روش نیازی به قرار داشتن در چت بات نیست و می توانید در چت های خصوصی و یا گروه ها و کانال ها از آن استفاده کنید."
  }

  private val mainMenuButtons = List(
    List((MainMenu.FIND_PLACE_TITLE, MainMenu.FROM_FIND_PLACE)),
    List((MainMenu.SEARCH_PLACE_TITLE, MainMenu.FROM_SEARCH_PLACE)),
    List((MainMenu.ADD_PLACE_TITLE, MainMenu.FROM_ADD_PLACE)),
    List((MainMenu.EDIT_PLACE_TITLE, MainMenu.FROM_EDIT_PLACE)) //,
//    List((MainMenu.HELP_TITLE, MainMenu.FROM_HELP))
  )

  private val backToMainMenuButtons = List(List(
    (MainMenu.BACK_TO_MAIN_MENU_TITLE, MainMenu.FROM_BACK_TO_MAIN_MENU)
  ))

  private val distanceButtons = List(List(
    ((DistanceFilterMenu.FROM_THREE_KILOMETER_TITLE, DistanceFilterMenu.FROM_THREE_KILOMETER), 3d),
    ((DistanceFilterMenu.FROM_TWO_KILOMETER_TITLE, DistanceFilterMenu.FROM_TWO_KILOMETER), 2d),
    ((DistanceFilterMenu.FROM_ONE_KILOMETER_TITLE, DistanceFilterMenu.FROM_ONE_KILOMETER), 1d)
  ),
    List(
      ((DistanceFilterMenu.FROM_MORE_TITLE, DistanceFilterMenu.FROM_MORE), 1d),
      ((DistanceFilterMenu.FROM_FIVE_KILOMETER_TITLE, DistanceFilterMenu.FROM_FIVE_KILOMETER), 5d),
      ((DistanceFilterMenu.FROM_FOUR_KILOMETER_TITLE, DistanceFilterMenu.FROM_FOUR_KILOMETER), 4d)
    )
  ) ++ backToMainMenuButtons.map(_.zip(List(1d)))

  private val pointsButtons = List(List(
    (PointMenu.FROM_BAD_TITLE, PointMenu.FROM_BAD),
    (PointMenu.FROM_NORMAL_TITLE, PointMenu.FROM_NORMAL),
    (PointMenu.FROM_EXCELLENT_TITLE, PointMenu.FROM_EXCELLENT)
  ),
    List((PointMenu.FROM_SHOW_FIRST_POINT_TITLE, PointMenu.FROM_SHOW_FIRST_POINT),
      (PointMenu.FROM_SHOW_PREVIEW_POINT_TITLE, PointMenu.FROM_SHOW_PREVIEW_POINT),
      (PointMenu.FROM_SHOW_NUMBER_OF_POINT_TITLE, PointMenu.FROM_SHOW_NUMBER_OF_POINT),
      (PointMenu.FROM_SHOW_NEXT_POINT_TITLE, PointMenu.FROM_SHOW_NEXT_POINT),
      (PointMenu.FROM_SHOW_LAST_POINT_TITLE, PointMenu.FROM_SHOW_LAST_POINT)
    )) ++ backToMainMenuButtons

  private val placeButtons = List(
    List(
      (PlaceMenu.FROM_PLACE_LIKE_TITLE, PlaceMenu.FROM_PLACE_LIKE),
      (PlaceMenu.FROM_PLACE_DIS_LIKE_TITLE, PlaceMenu.FROM_PLACE_DISLIKE),
      (PlaceMenu.FROM_PLACE_SET_POINT_TITLE, PlaceMenu.FROM_PLACE_SET_POINT)
    ),
    List(
      (PlaceMenu.FROM_PLACE_SET_COMMENT_TITLE, PlaceMenu.FROM_PLACE_SET_COMMENT),
      (PlaceMenu.FROM_PLACE_SHOW_COMMENT_TITLE, PlaceMenu.FROM_PLACE_SHOW_COMMENT)
    )
  ) ++ backToMainMenuButtons

  private val previewPlaceButtons = List(List((PlaceMenu.FROM_SHOW_FIRST_PLACE_TITLE, PlaceMenu.FROM_SHOW_FIRST_PLACE),
    (PlaceMenu.FROM_SHOW_PREVIEW_PLACE_TITLE, PlaceMenu.FROM_SHOW_PREVIEW_PLACE),
    (PlaceMenu.FROM_SHOW_NUMBER_OF_PLACE_TITLE, PlaceMenu.FROM_SHOW_NUMBER_OF_PLACE),
    (PlaceMenu.FROM_SHOW_NEXT_PLACE_TITLE, PlaceMenu.FROM_SHOW_NEXT_PLACE),
    (PlaceMenu.FROM_SHOW_LAST_PLACE_TITLE, PlaceMenu.FROM_SHOW_LAST_PLACE)
  )) ++ backToMainMenuButtons

  private val locationButtons = List(List(
    (LocationMenu.GET_LOCATION_TITLE, LocationMenu.FROM_GET_LOCATION)
  ))

  private val showCommentButtons = List(List(
    (CommentMenu.FROM_LIKE_COMMENT_TITLE, CommentMenu.FROM_LIKE_COMMENT),
    (CommentMenu.FROM_DISLIKE_COMMENT_TITLE, CommentMenu.FROM_DISLIKE_COMMENT)
  ),
    List((CommentMenu.FROM_SHOW_FIRST_COMMENT_TITLE, CommentMenu.FROM_SHOW_FIRST_COMMENT),
      (CommentMenu.FROM_SHOW_PREVIEW_COMMENT_TITLE, CommentMenu.FROM_SHOW_PREVIEW_COMMENT),
      (CommentMenu.FROM_SHOW_NUMBER_OF_COMMENT_TITLE, CommentMenu.FROM_SHOW_NUMBER_OF_COMMENT),
      (CommentMenu.FROM_SHOW_NEXT_COMMENT_TITLE, CommentMenu.FROM_SHOW_NEXT_COMMENT),
      (CommentMenu.FROM_SHOW_LAST_COMMENT_TITLE, CommentMenu.FROM_SHOW_LAST_COMMENT)
    )) ++ backToMainMenuButtons

  def getCallbackMessageId(callbackQuery: CallbackQuery): Option[Int] = {
    callbackQuery.message.flatMap {
      _.asJsObject.getFields("message_id") match {
        case Seq(JsNumber(value)) => Some(value.toInt)
        case _ => None
      }
    }
  }

  object FromButton {
    val PLACE = "place"
    val PLACE_LIST = "place-list"
    val REVIEW = "review"
    val POINT = "point"
    val SET_LOCATION = "set-location"
  }

  case class SelectGuild(callbackQuery: CallbackQuery, guildId: Long)

  case class SelectFilter(callbackQuery: CallbackQuery, subGuildId: Option[Long])

  case class SelectDistance(callbackQuery: CallbackQuery, subGuildId: Option[Long])

  case class SelectNearestVenues(user: User, subGuildId: Long, distance: Double)

  case class SelectVenueLikeButton(callbackQuery: CallbackQuery)

  case class SelectVenueDislikeButton(callbackQuery: CallbackQuery)

  case class SelectVenueSetPointButton(callbackQuery: CallbackQuery)

  case class SelectVenueSetCommentButton(callbackQuery: CallbackQuery)

  case class SelectVenueShowCommentButton(callbackQuery: CallbackQuery)

  case class SelectShowReviewButton(messageId: Int, user: User, reviewId: Option[Long], placeId: Long, placeTitle: String, comment: String, likes: Int, dislikes: Int, page: Int, totalPage: Int, last: Boolean, from: String)

  case class SelectGetLocationButton(callbackQuery: CallbackQuery, subGuildId: Option[Long])

  case class CreatePointQuestion(messageId: Int, user: User, placeId: Long, placeTitle: String, question: UserQuestion, page: Int, totalPage: Int, last: Boolean, from: String)

  case class BackToMainMenu(callbackQuery: CallbackQuery)

}