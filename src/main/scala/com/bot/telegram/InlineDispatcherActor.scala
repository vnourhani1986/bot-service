package com.bot.telegram

import akka.actor.{Actor, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.bot.models.json.bot.telegram._
import com.bot.models.repo.city.City
import com.bot.models.repo.guild.SubGuild
import com.bot.models.repo.place.{Place, QueryPlace}
import com.bot.services.telegram.TelegramBotService.Messages._
import com.bot.services.telegram.TelegramBotService._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext

class InlineDispatcherActor(
                             implicit
                             system: ActorSystem,
                             ec: ExecutionContext,
                             timeout: Timeout
                           ) extends Actor with LazyLogging {

  self ! Start()

  override def receive(): Receive = {

    case Start() =>
      logger.info(s"""inline dispatcher actor is started""")

    case SendInlineQueryToDispatcher(inlineQuery) =>

      userActor ! SendQueryToUserActor(inlineQuery)

    case InlineQueryFromCity(inlineQuery) =>

      for {
        queryCities <- (postgresActor ? QueryFromCity(inlineQuery.query)).mapTo[Seq[City]]
      } yield {
        senderActor ! AnswerInlineQueryArticle(inlineQuery.id,
          queryCities.map { qCity =>
            InlineQueryResultArticle(
              `type` = "article",
              id = qCity.id.get.toString,
              title = qCity.faName,
              InputTextMessageContent(
                message_text = qCity.faName
              )
            )
          }.toList
        )
      }

    case InlineQueryFromSubGuild(inlineQuery) =>

      for {
        querySubGuilds <- (postgresActor ? QueryFromSubGuild(inlineQuery.query)).mapTo[Seq[SubGuild]]
      } yield {
        senderActor ! AnswerInlineQueryArticle(inlineQuery.id,
          querySubGuilds.map { qSubGuild =>
            InlineQueryResultArticle(
              `type` = "article",
              id = qSubGuild.id.get.toString,
              title = qSubGuild.title.getOrElse(""),
              InputTextMessageContent(
                message_text = qSubGuild.title.getOrElse("")
              )
            )
          }.toList
        )
      }

    case InlineQueryFromUserPlace(inlineQuery) =>

      for {
        places <- (postgresActor ? QueryFromUserPlace(inlineQuery.from.id, inlineQuery.query)).mapTo[Seq[Place]]
      } yield {
        senderActor ! AnswerInlineQueryArticle(inlineQuery.id,
          places.map { place =>
            InlineQueryResultArticle(
              `type` = "article",
              id = place.id.get.toString,
              title = place.title.getOrElse(""),
              InputTextMessageContent(
                message_text = place.title.getOrElse("")
              )
            )
          }.toList
        )
      }

    case InlineQueryFromPlace(inlineQuery) =>

      for {
        queryPlaces <- (findPlaceActor ? SendInlineQueryToFindPlaceActor(inlineQuery.query)).mapTo[Seq[QueryPlace]]
      } yield {
        senderActor ! AnswerInlineQueryVenue(inlineQuery.id,
          queryPlaces.map { qPlace =>
            InlineQueryResultVenue("venue",
              qPlace.id.get.toString,
              qPlace.lat.getOrElse(0d).toFloat,
              qPlace.lng.getOrElse(0d).toFloat,
              s"""${qPlace.title.getOrElse("")}""",
              qPlace.address.getOrElse(""))
          }.toList
        )
      }

    case SendChosenInlineResultToDispatcher(chosenInlineResult) =>

      userActor ! SendChosenInlineResultToUserActor(chosenInlineResult)

    case SendCityToDispatcherActor(chosenInlineResult) =>

      addPlaceRouterActor ! SendInlineCityResult(chosenInlineResult)

    case SendSubGuildToDispatcherActor(chosenInlineResult) =>

      addPlaceRouterActor ! SendInlineSubGuildResult(chosenInlineResult)

    case SendPlaceToDispatcherActor(chosenInlineResult) =>

      editPlaceActor ! SendInlinePlaceResult(chosenInlineResult)

    case SendEditPlaceCityToDispatcherActor(chosenInlineResult) =>

      editPlaceActor ! SendInlineCityResult(chosenInlineResult)

    case SendEditPlaceSubGuildToDispatcherActor(chosenInlineResult) =>

      editPlaceActor ! SendInlineSubGuildResult(chosenInlineResult)

    case _ =>
      logger.info(s"""welcome to inline dispatcher actor""")

  }

}
