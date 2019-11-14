package com.bot.services.telegram

import java.time.LocalDateTime

import akka.pattern.ask
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.routing.{DefaultResizer, RoundRobinPool}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.bot.DI.{ec, materializer, system}
import com.bot.models.repo.guild.Guild
import com.bot.models.repo.place.Place
import com.bot.models.repo.user.UserActivity
import com.bot.services.telegram.TelegramBotService.Messages.SendIndexToElasticSearchActor
import com.bot.telegram.{CommandActor, _}
import spray.json.{JsNumber, JsObject, JsValue}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Random

class TelegramBotService(
                          implicit
                          system: ActorSystem,
                          ec: ExecutionContext,
                          timeout: Timeout,
                          mat: ActorMaterializer
                        ) {

  val resizer = DefaultResizer(lowerBound = 2, upperBound = 25)
  val updaterActor: ActorRef = system.actorOf(Props(new UpdaterActor), s"telegram-updater-actor-${Random.nextInt}")
  val messageDispatcherActor: ActorRef = system.actorOf(RoundRobinPool(10, Some(resizer)).props(Props(new MessageDispatcherActor)), s"telegram-message-dispatcher-actor-${Random.nextInt}")
  val commandActor: ActorRef = system.actorOf(Props(new CommandActor), s"telegram-command-actor-${Random.nextInt}")
  val inlineDispatcherActor: ActorRef = system.actorOf(RoundRobinPool(10, Some(resizer)).props(Props(new InlineDispatcherActor)), s"telegram-inline-dispatcher-actor-${Random.nextInt}")
  val callbackDispatcherActor: ActorRef = system.actorOf(RoundRobinPool(10, Some(resizer)).props(Props(new CallbackDispatcherActor)), s"telegram-callback-dispatcher-actor-${Random.nextInt}")
  val shippingDispatcherActor: ActorRef = system.actorOf(Props(new ShippingDispatcherActor), s"telegram-shipping-dispatcher-actor-${Random.nextInt}")
  val preCheckoutDispatcherActor: ActorRef = system.actorOf(Props(new PreCheckoutDispatcherActor), s"telegram-pre-checkout-dispatcher-actor-${Random.nextInt}")
  val pollDispatcherActor: ActorRef = system.actorOf(Props(new PollDispatcherActor), s"telegram-poll-dispatcher-actor-${Random.nextInt}")
  val postgresActor: ActorRef = system.actorOf(RoundRobinPool(20, Some(resizer)).props(Props(new PostgresActor())), s"telegram-postgres-actor-${Random.nextInt}")
  val osrmActor: ActorRef = system.actorOf(RoundRobinPool(10, Some(resizer)).props(Props(new OSRMActor)), s"telegram-osrm-actor-${Random.nextInt}")
  val geoCalcActor: ActorRef = system.actorOf(Props(new GEOCalcActor), s"telegram-geo-calculator-Actor-${Random.nextInt}")
  val senderActor: ActorRef = system.actorOf(RoundRobinPool(20, Some(resizer)).props(Props(new SenderActor)), s"telegram-send-actor-${Random.nextInt}")
  val userActor: ActorRef = system.actorOf(RoundRobinPool(20, Some(resizer)).props(Props(new UserActor)), s"telegram-user-actor-${Random.nextInt}")
  val findPlaceActor: ActorRef = system.actorOf(RoundRobinPool(10, Some(resizer)).props(Props(new FindPlaceActor)), s"telegram-place-actor-${Random.nextInt}")
  val addPlaceRouterActor: ActorRef = system.actorOf(Props(new AddPlaceRouterActor), s"telegram-add-place-router-actor-${Random.nextInt}")
  val editPlaceActor: ActorRef = system.actorOf(Props(new EditPlaceActor), s"telegram-edit-place-actor-${Random.nextInt}")
  val healthCheckActor: ActorRef = system.actorOf(Props(new HealthCheckActor()), s"telegram-health-check-actor-${Random.nextInt}")
  val elasticSearchActor: ActorRef = system.actorOf(RoundRobinPool(20, Some(resizer)).props(Props(new ElasticSearchActor())), s"telegram-elastic-search-actor-${Random.nextInt}")

}

object TelegramBotService {

  private implicit val timeout: Timeout = Timeout(1.minute)
  val service: TelegramBotService = new TelegramBotService
  val updaterActor: ActorRef = service.updaterActor
  val messageDispatcherActor: ActorRef = service.messageDispatcherActor
  val commandActor: ActorRef = service.commandActor
  val inlineDispatcherActor: ActorRef = service.inlineDispatcherActor
  val callbackDispatcherActor: ActorRef = service.callbackDispatcherActor
  val shippingDispatcherActor: ActorRef = service.shippingDispatcherActor
  val preCheckoutDispatcherActor: ActorRef = service.preCheckoutDispatcherActor
  val pollDispatcherActor: ActorRef = service.pollDispatcherActor
  val postgresActor: ActorRef = service.postgresActor
  val osrmActor: ActorRef = service.osrmActor
  val geoCalcActor: ActorRef = service.geoCalcActor
  val senderActor: ActorRef = service.senderActor
  val userActor: ActorRef = service.userActor
  val findPlaceActor: ActorRef = service.findPlaceActor
  val addPlaceRouterActor: ActorRef = service.addPlaceRouterActor
  val editPlaceActor: ActorRef = service.editPlaceActor
  val healthCheckActor: ActorRef = service.healthCheckActor
  val elasticSearchActor: ActorRef = service.elasticSearchActor

  object Messages {

    import com.bot.models._
    import com.bot.models.json.bot.telegram._

    case class Start()

    case class StartCommand(chatId: Int)

    case class CreateMainMenu(chatId: Int)

    case class CreateBackToMainMenu(chatId: Int, photo: String, caption: String)

    case class GetUpdates()

    case class SaveUserInfo(user: repo.user.User)

    case class SaveUserLocation(userId: Long, location: Location)

    case class SavePlaceLike(userId: Long, placeId: Long, like: Boolean)

    case class SaveReviewLike(userId: Long, reviewId: Long, like: Boolean)

    case class GetNearByDistanceCalc(messageId: Int, chatId: Int, subGuilds: List[Long], location: Location, radio: Double, page: Int, viewLength: Int, last: Boolean, from: String)

    case class GetPlace(messageId: Int, chatId: Int, placeId: Long, from: String)

    case class SendInlineQueryToFindPlaceActor(query: String)

    case class SendApiQueryToFindPlaceActor(query: String, lat: Option[Double], lng: Option[Double], page: Int)

    case class GetNearFromOSRM(messagrId: Int, chatId: Int, subGuilds: List[Long], location: Location, radio: Double, page: Int, viewLength: Int, last: Boolean, from: String)

    case class GetPopPlaces(messagrId: Int, chatId: Int, subGuilds: List[Long], location: Location, radio: Double, page: Int, totalPage: Int, last: Boolean, from: String)

    case class GetLastActivity(userId: Int, noAction: String = "")

    case class GetLastActivityForSpecificAction(userId: Int, action: String)

    case class GetLastLocation(userId: Int)

    case class SendMessageToDispatcher(message: Message)

    case class SendEditedMessageToDispatcher(message: Message)

    case class SendChannelPostToDispatcher(message: Message)

    case class SendEditedChannelPostToDispatcher(message: Message)

    case class SendInlineQueryToDispatcher(inlineQuery: InlineQuery)

    case class SendChosenInlineResultToDispatcher(chosenInlineResult: ChosenInlineResult)

    case class SendCallbackQueryToDispatcher(callbackQuery: CallbackQuery)

    case class SendUserToDispatcher(user: User)

    case class SendShippingQueryToDispatcher(shippingQuery: ShippingQuery)

    case class SendPreCheckoutQueryToDispatcher(preCheckoutQuery: PreCheckoutQuery)

    case class SendPollToDispatcher(poll: Poll)

    case class SendQueryToUserActor(inlineQuery: InlineQuery)

    case class InlineQueryFromCity(inlineQuery: InlineQuery)

    case class QueryFromCity(query: String)

    case class InlineQueryFromSubGuild(inlineQuery: InlineQuery)

    case class InlineQueryFromUserPlace(inlineQuery: InlineQuery)

    case class QueryFromSubGuild(query: String)

    case class InlineQueryFromPlace(inlineQuery: InlineQuery)

    case class SendChosenInlineResultToUserActor(chosenInlineResult: ChosenInlineResult)

    case class SendCityToDispatcherActor(chosenInlineResult: ChosenInlineResult)

    case class SendEditPlaceCityToDispatcherActor(chosenInlineResult: ChosenInlineResult)

    case class SendPlaceToDispatcherActor(chosenInlineResult: ChosenInlineResult)

    case class SendSubGuildToDispatcherActor(chosenInlineResult: ChosenInlineResult)

    case class SendEditPlaceSubGuildToDispatcherActor(chosenInlineResult: ChosenInlineResult)

    case class SendInlineCityResult(chosenInlineResult: ChosenInlineResult)

    case class SendInlineSubGuildResult(chosenInlineResult: ChosenInlineResult)

    case class SendInlinePlaceResult(chosenInlineResult: ChosenInlineResult)

    case class GetGuilds()

    case class GetSubGuilds(guildId: Long)

    case class GetSubGuildId(title: String)

    case class GetCityId(title: String)

    case class GetFilters()

    case class GetQuestions(subGuildId: Long, offset: Int, length: Int)

    case class GetReview(take: Int, placeId: Long, last: Boolean)

    case class GetTotalReviews(placeId: Long)

    case class GetReviewTotalLikes(reviewId: Long)

    case class GetPlaceIdsFromZone(lat: Double, lng: Double, radio: Double, offset: Int, Length: Int)

    case class GetNearPlaces(subGuilds: List[Long], lat: Double, lng: Double, radio: Double, offset: Int, Length: Int)

    case class GetPlaceToPostgresActor(placeId: Long)

    case class GetBySubGuildAndIds(placeIds: Seq[Long], subGuilds: List[Long])

    case class GetPopPlaceBySubGuild(subGuilds: List[Long], lat: Double, lng: Double, radio: Double)

    case class FindQueryPlacesWithPlaceIds(keywords: String, placeIds: Seq[Long])

    case class FindQueryPlaceKeys(keywords: String)

    case class FindQueryPlaces(ids: Seq[Long])

    case class FindPlaceByQuery(query: String)

    case class FindPlaceByApiQuery(query: String, lat: Option[Double], lng: Option[Double], page: Int)

    case class FindPlacesWithLikes(place: Seq[Place])

    case class GetPlaces(placeIds: Seq[Long])

    case class GetPlaceFromPostgresActor(placeId: Long)

    case class GetDuration(source: (Float, Float), destination: List[(Float, Float)])

    case class SaveReview(userId: Long, placeId: Long, comment: String)

    case class SaveRate(userId: Long, placeId: Long, questionId: Long, rate: Int)

    case class FindSubGuildsByInlineQuery(query: String)

    case class Test()

    //
    case class SendGuildsToCallbackDispatcher(chat: Chat, guilds: Seq[Guild])

    case class SendFindPlaceToUserActor(user: User, metaData: JsValue)

    case class SendSearchPlaceToUserActor(user: User, metaData: JsValue)

    case class SendAddPlaceToUserActor(user: User, metaData: JsValue)

    case class SendEditPlaceToUserActor(user: User, metaData: JsValue)

    case class SendSelectedGuildToUserActor(user: User, guildId: Long, metaData: JsValue)

    case class SendSelectedSubGuildToUserActor(user: User, subGuildId: Long, metaData: JsValue)

    case class SendSelectedFilterToUserActor(user: User, subGuildId: Long, filterId: Long, metaData: JsValue)

    case class SendSelectedDistanceFilterToUserActor(user: User, subGuildId: Long, filterId: Long, distance: Double, metaData: JsValue)

    case class SendCreateVenueToCallbackActor(messageId: Int, chatId: Int, places: Seq[(Place, Int, Int)], from: String)

    case class SendPlaceListToCallbackActor(messageId: Int, chatId: Int, places: Seq[Place], page: Int, totalPage: Int, last: Boolean, from: String)

    case class SendPlaceLikeToUserActor(user: User, placeId: Long, like: Boolean, metaData: JsValue)

    case class SendReviewLikeToUserActor(user: User, reviewId: Long, like: Boolean, metaData: JsValue)

    case class SendShowReviewToUserActor(messageId: Int, user: User, placeId: Long, page: Int, metaData: JsValue, last: Boolean = false, action: String, from: String)

    case class SendSetReviewToUserActor(user: User, placeId: Long, metaData: JsValue)

    case class SendTextToUserActor(chat: Chat, user: Option[User], text: String)

    case class SendSelectedPointToUserActor(messageId: Int, user: User, placeId: Long, metaData: JsValue, page: Int, last: Boolean, action: String, from: String)

    case class SendOtherPagePlaceListToUserActor(messageId: Int, user: User, metaData: JsValue, page: Int, last: Boolean, action: String, from: String)

    case class SendSelectedPlaceToUserActor(messageId: Int, user: User, placeId: Long, metaData: JsValue, action: String, from: String)

    case class SendSelectedPointItemToUserActor(user: User, placeId: Long, questionId: Long, rate: Int, metaData: JsValue)

    case class SendLocationToUserActor(messageId: Int, chat: Chat, user: Option[User], location: Location)

    case class SendCommandToUserActor(chat: Chat, user: Option[User], command: String)

    case class SendCommandToCommandActor(chat: Chat, command: String)

    case class SendUsernameToUserActor(chat: Chat, user: Option[User], username: String)

    case class SaveUserActivity(userActivity: UserActivity)

    case class SendStartCommandToCommandActor(chat: Chat, command: String)

    case class SendAddPlaceToAddPlaceRouterActor(user: User)

    case class SendAddPlaceWelcomeToCallbackDispatcher(user: User, caption: String, keyboardTitle: String)

    case class SendStartAddPlaceToUserActor(user: User)

    case class SendStartAddPlaceToAddPlaceRouterActor(user: User)

    case class SendTextToAddPlaceActor(chat: Chat, user: Option[User], text: String, lastAction: String)

    case class SendTextToEditPlaceActor(chat: Chat, user: Option[User], text: String, lastAction: String)

    case class SendLocationToAddPlaceActor(chat: Chat, user: Option[User], location: Location)

    case class SendLocationToEditPlaceActor(chat: Chat, user: Option[User], location: Location)

    case class AddPlaceToPostgresActor(userId: Long, cityId: Long, subGuildId: Long, title: String, address: String, phone: String, lat: Float, lng: Float, link: Option[String])

    case class SendEditPlaceToEditPlaceActor(user: User)

    case class SendEditPlaceTitleToEditPlaceActor(chat: Chat)

    case class SendEditPlaceAddressToEditPlaceActor(chat: Chat)

    case class SendEditPlacePhoneToEditPlaceActor(chat: Chat)

    case class SendEditPlaceLocationToEditPlaceActor(chat: Chat)

    case class SendEditPlaceCityToEditPlaceActor(chat: Chat)

    case class SendEditPlaceSubGuildToEditPlaceActor(chat: Chat)

    case class SendEditPlaceLinkToEditPlaceActor(chat: Chat)

    case class QueryFromUserPlace(userId: Long, query: String)

    case class UpdatePlace(placeId: Long, title: Option[String] = None, address: Option[String] = None, phone: Option[String] = None, location: Option[Location] = None, cityId: Option[Long] = None, subGuildId: Option[Long] = None, link: Option[String] = None)

    case class SendIndexToElasticSearchActor(index: JsValue, url: String)

    case class SendErrorToElasticSearchActor(index: JsValue, url: String)

    case class ElasticSearchError(error: String, createdAt: LocalDateTime)

  }

}
