package com.bot.telegram

import akka.actor.{Actor, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.pattern.ask
import akka.util.Timeout
import com.bot.formats.Formats._
import com.bot.models.json.bot.telegram.{Chat, SendMessage, SendPhoto, User}
import com.bot.models.repo.user.UserActivity
import com.bot.services.telegram.TelegramBotService.Messages._
import com.bot.services.telegram.TelegramBotService.{callbackDispatcherActor, elasticSearchActor, postgresActor, senderActor, userActor}
import com.bot.telegram.CommandActor.Commands
import com.bot.telegram.EditPlaceActor._
import com.bot.telegram.UserActor.{Action, SaveUserActivityWithUserInfo, SaveUserActivityWithUserInfoWithoutChatInfo}
import com.bot.utils.{DateTimeUtils, TelegramBotConfig}
import com.bot.utils.formatters.MobileNoFormatter.format
import com.typesafe.scalalogging.LazyLogging
import spray.json._

import scala.concurrent.{ExecutionContext, Future}

class EditPlaceActor(
                      implicit
                      system: ActorSystem,
                      ec: ExecutionContext,
                      timeout: Timeout
                    ) extends Actor with LazyLogging {

  self ! Start()

  override def receive(): Receive = {

    case Start() =>
      logger.info(s"""edit place actor is started""")

    case SendEditPlaceToEditPlaceActor(user) =>

      senderActor ! SendPhoto(chat_id = user.id, photo = TelegramBotConfig.editPlacePhoto, caption = Some(Contents.GO_TO_EDIT_PLACE))

    case SendInlinePlaceResult(chosenInlineResult) =>

      senderActor ! SendPhoto(chat_id = chosenInlineResult.from.id,
        photo = TelegramBotConfig.selectFieldEditPlacePhoto,
        caption = Some(
          s"""${Contents.START_EDIT_PLACE}
             |<a>${Commands.EDIT_PLACE_TITLE}</a>
             |<a>${Commands.EDIT_PLACE_ADDRESS}</a>
             |<a>${Commands.EDIT_PLACE_PHONE}</a>
             |<a>${Commands.EDIT_PLACE_LOCATION}</a>
             |<a>${Commands.EDIT_PLACE_CITY_TITLE}</a>
             |<a>${Commands.EDIT_PLACE_SUB_GUILD_TITLE}</a>
             |<a>${Commands.EDIT_PLACE_LINK}</a>""".stripMargin),
        parse_mode = Some("HTML"))

    case SendEditPlaceTitleToEditPlaceActor(chat) =>

      senderActor ! SendMessage(chat.id, Contents.EDIT_PLACE_ASK_TITLE)

    case SendEditPlaceAddressToEditPlaceActor(chat) =>

      senderActor ! SendMessage(chat.id, Contents.EDIT_PLACE_ASK_ADDRESS)

    case SendEditPlacePhoneToEditPlaceActor(chat) =>

      senderActor ! SendMessage(chat.id, Contents.EDIT_PLACE_ASK_PHONE)

    case SendEditPlaceLocationToEditPlaceActor(chat) =>

      senderActor ! SendMessage(chat.id,
        s"""${Contents.EDIT_PLACE_ASK_LOCATION_1}
           |${Contents.EDIT_PLACE_ASK_LOCATION_2}
           |${Contents.EDIT_PLACE_ASK_LOCATION_3}
           |<a>${Contents.EDIT_PLACE_ASK_LOCATION_4}</a>
           |${Contents.EDIT_PLACE_ASK_LOCATION_5}
           |<a>${Contents.EDIT_PLACE_ASK_LOCATION_6}</a>
           |${Contents.EDIT_PLACE_ASK_LOCATION_7}
         """.stripMargin,
        parse_mode = Some("HTML"))

    case SendEditPlaceCityToEditPlaceActor(chat) =>

      senderActor ! SendMessage(chat.id, Contents.EDIT_PLACE_ASK_CITY_TITLE)

    case SendEditPlaceSubGuildToEditPlaceActor(chat) =>

      senderActor ! SendMessage(chat.id, Contents.EDIT_PLACE_ASK_SUB_GUILD_TITLE)

    case SendEditPlaceLinkToEditPlaceActor(chat) =>

      senderActor ! SendMessage(chat.id, Contents.EDIT_PLACE_ASK_LINK)

    case SendTextToEditPlaceActor(chat, user, text, lastAction) =>

      lastAction match {
        case Action.EDIT_PLACE_TITLE => self ! SetTitle(chat, user, text)
        case Action.EDIT_PLACE_ADDRESS => self ! SetAddress(chat, user, text)
        case Action.EDIT_PLACE_PHONE => self ! SetPhone(chat, user, text)
        case Action.EDIT_PLACE_LINK => self ! SetLink(chat, user, text)
      }

    case SetTitle(chat, user, text) =>

      (for {
        lastActivity <- (postgresActor ? GetLastActivityForSpecificAction(user.get.id, Action.START_EDIT_PLACE)).mapTo[Option[UserActivity]]
        _ <- lastActivity.flatMap(_.entityId) match {
          case Some(placeId) =>
            for {
              updateId <- (postgresActor ? UpdatePlace(placeId, title = Some(text))).mapTo[(Option[Long], Option[Long], Option[Long])]
              _ <- updateId match {
                case (Some(_), Some(_), Some(_)) => Future.successful()
                case (None, None, None) => Future.failed(new Exception("place did not updated"))
              }
              _ <- (userActor ? SaveUserActivityWithUserInfo(user.get, chat, Action.EDITED_PLACE_TITLE, None)).mapTo[UserActivity]
            } yield {

            }
          case None =>
            Future.failed(new Exception("no entity id detected"))
        }
      } yield {
        callbackDispatcherActor ! CreateBackToMainMenu(chat.id, TelegramBotConfig.successPhoto, Contents.EDITED)
      }).recover {
        case error: Throwable =>
          logger.info(s"""set title in edit place with error: ${error.getMessage}""")
          elasticSearchActor ! SendErrorToElasticSearchActor(
            ElasticSearchError(
              s"""set title in edit place with result:${error.getMessage} with status: ${StatusCodes.InternalServerError}""", DateTimeUtils.now).toJson,
            s"""/error_idx/error""")
          callbackDispatcherActor ! CreateBackToMainMenu(chat.id, TelegramBotConfig.failPhoto, Contents.EDIT_PLACE_ERROR)
      }

    case SetAddress(chat, user, text) =>

      (for {
        lastActivity <- (postgresActor ? GetLastActivityForSpecificAction(user.get.id, Action.START_EDIT_PLACE)).mapTo[Option[UserActivity]]
        _ <- lastActivity.flatMap(_.entityId) match {
          case Some(placeId) =>
            for {
              updateId <- (postgresActor ? UpdatePlace(placeId, address = Some(text))).mapTo[(Option[Long], Option[Long], Option[Long])]
              _ <- updateId match {
                case (Some(_), Some(_), Some(_)) => Future.successful()
                case (None, None, None) => Future.failed(new Exception("place did not updated"))
              }
              _ <- (userActor ? SaveUserActivityWithUserInfo(user.get, chat, Action.EDITED_PLACE_ADDRESS, None)).mapTo[UserActivity]
            } yield {

            }
          case None =>
            Future.failed(new Exception("no entity id detected"))
        }
      } yield {
        callbackDispatcherActor ! CreateBackToMainMenu(chat.id, TelegramBotConfig.successPhoto, Contents.EDITED)
      }).recover {
        case error: Throwable =>
          logger.info(s"""set address in edit place with error: ${error.getMessage}""")
          elasticSearchActor ! SendErrorToElasticSearchActor(
            ElasticSearchError(
              s"""set address in edit place with error: ${error.getMessage}""", DateTimeUtils.now).toJson,
            s"""/error_idx/error""")
          callbackDispatcherActor ! CreateBackToMainMenu(chat.id, TelegramBotConfig.failPhoto, Contents.EDIT_PLACE_ERROR)
      }

    case SetPhone(chat, user, text) =>

      (for {
        lastActivity <- (postgresActor ? GetLastActivityForSpecificAction(user.get.id, Action.START_EDIT_PLACE)).mapTo[Option[UserActivity]]
        _ <- lastActivity.flatMap(_.entityId) match {
          case Some(placeId) =>
            for {
              updateId <- (postgresActor ? UpdatePlace(placeId, phone = format(Some(text)))).mapTo[(Option[Long], Option[Long], Option[Long])]
              _ <- updateId match {
                case (Some(_), Some(_), Some(_)) => Future.successful()
                case (None, None, None) => Future.failed(new Exception("place did not updated"))
              }
              _ <- (userActor ? SaveUserActivityWithUserInfo(user.get, chat, Action.EDITED_PLACE_PHONE, None)).mapTo[UserActivity]
            } yield {

            }
          case None =>
            Future.failed(new Exception("no entity id detected"))
        }
      } yield {
        callbackDispatcherActor ! CreateBackToMainMenu(chat.id, TelegramBotConfig.successPhoto, Contents.EDITED)
      }).recover {
        case error: Throwable =>
          logger.info(s"""set phone in edit place with error: ${error.getMessage}""")
          elasticSearchActor ! SendErrorToElasticSearchActor(
            ElasticSearchError(
              s"""set phone in edit place with error: ${error.getMessage}""", DateTimeUtils.now).toJson,
            s"""/error_idx/error""")
          callbackDispatcherActor ! CreateBackToMainMenu(chat.id, TelegramBotConfig.failPhoto, Contents.EDIT_PLACE_ERROR)
      }

    case SetLink(chat, user, text) =>

      (for {
        lastActivity <- (postgresActor ? GetLastActivityForSpecificAction(user.get.id, Action.START_EDIT_PLACE)).mapTo[Option[UserActivity]]
        _ <- lastActivity.flatMap(_.entityId) match {
          case Some(placeId) =>
            for {
              updateId <- (postgresActor ? UpdatePlace(placeId, link = Some(text))).mapTo[(Option[Long], Option[Long], Option[Long])]
              _ <- updateId match {
                case (Some(_), Some(_), Some(_)) => Future.successful()
                case (None, None, None) => Future.failed(new Exception("place did not updated"))
              }
              _ <- (userActor ? SaveUserActivityWithUserInfo(user.get, chat, Action.EDITED_PLACE_LINK, None)).mapTo[UserActivity]
            } yield {

            }
          case None =>
            Future.failed(new Exception("no entity id detected"))
        }
      } yield {
        callbackDispatcherActor ! CreateBackToMainMenu(chat.id, TelegramBotConfig.successPhoto, Contents.EDITED)
      }).recover {
        case error: Throwable =>
          logger.info(s"""set link in edit place with error: ${error.getMessage}""")
          elasticSearchActor ! SendErrorToElasticSearchActor(
            ElasticSearchError(
              s"""set link in edit place with error: ${error.getMessage}""", DateTimeUtils.now).toJson,
            s"""/error_idx/error""")
          callbackDispatcherActor ! CreateBackToMainMenu(chat.id, TelegramBotConfig.failPhoto, Contents.EDIT_PLACE_ERROR)
      }

    case SendLocationToEditPlaceActor(chat, user, location) =>

      (for {
        lastActivity <- (postgresActor ? GetLastActivityForSpecificAction(user.get.id, Action.START_EDIT_PLACE)).mapTo[Option[UserActivity]]
        _ <- lastActivity.flatMap(_.entityId) match {
          case Some(placeId) =>
            for {
              updateId <- (postgresActor ? UpdatePlace(placeId, location = Some(location))).mapTo[(Option[Long], Option[Long], Option[Long])]
              _ <- updateId match {
                case (Some(_), Some(_), Some(_)) => Future.successful()
                case (None, None, None) => Future.failed(new Exception("place did not updated"))
              }
              _ <- (userActor ? SaveUserActivityWithUserInfo(user.get, chat, Action.EDITED_PLACE_LOCATION, None)).mapTo[UserActivity]
            } yield {

            }
          case None =>
            Future.failed(new Exception("no entity id detected"))
        }
      } yield {
        callbackDispatcherActor ! CreateBackToMainMenu(chat.id, TelegramBotConfig.successPhoto, Contents.EDITED)
      }).recover {
        case error: Throwable =>
          logger.info(s"""set location in edit place with error: ${error.getMessage}""")
          elasticSearchActor ! SendErrorToElasticSearchActor(
            ElasticSearchError(
              s"""set location in edit place with error: ${error.getMessage}""", DateTimeUtils.now).toJson,
            s"""/error_idx/error""")
          callbackDispatcherActor ! CreateBackToMainMenu(chat.id, TelegramBotConfig.failPhoto, Contents.EDIT_PLACE_ERROR)
      }

    case SendInlineCityResult(chosenInlineResult) =>

      val user = chosenInlineResult.from
      val cityId = chosenInlineResult.result_id.toLong
      (for {
        lastActivity <- (postgresActor ? GetLastActivityForSpecificAction(user.id, Action.START_EDIT_PLACE)).mapTo[Option[UserActivity]]
        _ <- lastActivity.flatMap(_.entityId) match {
          case Some(placeId) =>
            for {
              updateId <- (postgresActor ? UpdatePlace(placeId, cityId = Some(cityId))).mapTo[(Option[Long], Option[Long], Option[Long])]
              _ <- updateId match {
                case (Some(_), Some(_), Some(_)) => Future.successful()
                case (None, None, None) => Future.failed(new Exception("place did not updated"))
              }
              _ <- (userActor ? SaveUserActivityWithUserInfoWithoutChatInfo(user, Action.EDITED_PLACE_CITY_ID, None)).mapTo[UserActivity]
            } yield {

            }
          case None =>
            Future.failed(new Exception("no entity id detected"))
        }
      } yield {
        callbackDispatcherActor ! CreateBackToMainMenu(user.id, TelegramBotConfig.successPhoto, Contents.EDITED)
      }).recover {
        case error: Throwable =>
          logger.info(s"""set inline city result in edit place with error: ${error.getMessage}""", DateTimeUtils.now)
          elasticSearchActor ! SendErrorToElasticSearchActor(
            ElasticSearchError(
              s"""set inline city result in edit place with error: ${error.getMessage}""", DateTimeUtils.now).toJson,
            s"""/error_idx/error""")
          callbackDispatcherActor ! CreateBackToMainMenu(user.id, TelegramBotConfig.failPhoto, Contents.EDIT_PLACE_ERROR)
      }

    case SendInlineSubGuildResult(chosenInlineResult) =>

      val user = chosenInlineResult.from
      val subGuildId = chosenInlineResult.result_id.toLong
      (for {
        lastActivity <- (postgresActor ? GetLastActivityForSpecificAction(user.id, Action.START_EDIT_PLACE)).mapTo[Option[UserActivity]]
        _ <- lastActivity.flatMap(_.entityId) match {
          case Some(placeId) =>
            for {
              updateId <- (postgresActor ? UpdatePlace(placeId, subGuildId = Some(subGuildId))).mapTo[(Option[Long], Option[Long], Option[Long])]
              _ <- updateId match {
                case (Some(_), Some(_), Some(_)) => Future.successful()
                case (None, None, None) => Future.failed(new Exception("place did not updated"))
              }
              _ <- (userActor ? SaveUserActivityWithUserInfoWithoutChatInfo(user, Action.EDIT_PLACE_SUB_GUILD_ID, None)).mapTo[UserActivity]
            } yield {

            }
          case None =>
            Future.failed(new Exception("no entity id detected"))
        }
      } yield {
        callbackDispatcherActor ! CreateBackToMainMenu(user.id, TelegramBotConfig.successPhoto, Contents.EDITED)
      }).recover {
        case error: Throwable =>
          logger.info(s"""set inline sub guild result in edit place with error: ${error.getMessage}""")
          elasticSearchActor ! SendErrorToElasticSearchActor(
            ElasticSearchError(
              s"""set inline sub guild result in edit place with error: ${error.getMessage}""", DateTimeUtils.now).toJson,
            s"""/error_idx/error""")
          callbackDispatcherActor ! CreateBackToMainMenu(user.id, TelegramBotConfig.failPhoto, Contents.EDIT_PLACE_ERROR)
      }

    case _ =>
      logger.info(s"""welcome to edit place actor""")

  }

}

object EditPlaceActor {

  case class SetTitle(chat: Chat, user: Option[User], title: String)

  case class SetAddress(chat: Chat, user: Option[User], address: String)

  case class SetPhone(chat: Chat, user: Option[User], phone: String)

  case class SetLink(chat: Chat, user: Option[User], link: String)

  object Contents {
    val GO_TO_EDIT_PLACE = "در این قسمت می توانید کسب و کارهای ثبت شده توسط خود را ویرایش نمایید. تغییرات اعمال شده پس از بررسی توسط متخصصین ترینت برای کاربران قابل نمایش می باشند. برای شروع نام کسب و کاری را که ثبت کرده اید، انتخاب نمایید. جهت انجام این کار می بایست نام بات یعنی @my3netbot را در قسمت ورودی متن وارد کرده و در مقابل آن نام کسب و کار مربوطه را جستجو نمایید. در نهایت در لیست نمایش داده شده کسب و کار مورد نظر را انتخاب نمایید."
    val START_EDIT_PLACE = "لطفا نام مشخصه ای از کسب و کار که قسط تغییر آن را دارید از لیست زیر انتخاب نمایید."
    val EDIT_PLACE_ASK_CITY_TITLE = "لطفا نام شهری را که کسب و کار در آن قرار دارد وارد نمایید. جهت انجام این کار می بایست نام بات یعنی @t3netbot را در قسمت وارد کردن متن وارد کرده و در مقابل آن نام شهر محل کسب و کار را جستجو کنید. در نهایت در لیست نمایش داده شده شهر کسب و کار خود را انتخاب نمایید."
    val EDIT_PLACE_ASK_SUB_GUILD_TITLE = "لطفا نام صنفی که کسب و کار زیر مجموعه آن است را وارد نمایید. جهت انجام این کار می بایست نام بات یعنی @t3netbot را در قسمت وارد کردن متن وارد کرده و در مقابل آن نام صنف مربوط به کسب و کار را جستجو کنید. در نهایت در لیست نمایش داده شده صنف کسب و کار خود را انتخاب نمایید."
    val EDIT_PLACE_ASK_TITLE = "لطفا نام جدید کسب و کاری خود را وارد نمایید."
    val EDIT_PLACE_ASK_ADDRESS = "لطفا آدرس جدید محل کسب و کاری خود را وارد نمایید."
    val EDIT_PLACE_ASK_PHONE = "لطفا شماره تلفن جدید کسب و کاری خود را وارد نمایید."
    val EDIT_PLACE_ASK_LOCATION_1 = "لطفا لوکیشن جدید محل کسب و کاری خود را مشخص نمایید. دو روش جهت دریافت لوکیشن وجود دارد."
    val EDIT_PLACE_ASK_LOCATION_2 = "۱) انتخاب لوکیشن از طریق دکمه پایین صفحه: این روش تنها قابلیت ارسال لوکیشن فعلی شما به سرویس ترینت را دارد."
    val EDIT_PLACE_ASK_LOCATION_3 = "۲) استفاده از روش معمول در تلگرام: جهت آشنایی با چگونگی استفاده از این روش در گوشی های اندروید از لینک زیر"
    val EDIT_PLACE_ASK_LOCATION_4 = "https://www.youtube.com/watch?v=6lQVkQGm4aU"
    val EDIT_PLACE_ASK_LOCATION_5 = "و در گوشی های آی او اس از لینک زیر کمک بگیرید."
    val EDIT_PLACE_ASK_LOCATION_6 = "https://www.youtube.com/watch?v=e270rWBCYoc"
    val EDIT_PLACE_ASK_LOCATION_7 = "حال یکی از دو روش بالا را انتخاب نمایید تا لوکیشن محل کسب و کار ثبت شود."
    val EDIT_PLACE_ASK_LINK = "لطفا لینک جدید وب سایت کسب و کار خود را وارد نمایید."
    val EDIT_PLACE_ERROR = "اطلاعات وارد شده صحیح نمی باشند."
    val EDITED = "اطلاعات به درستی ثبت شد. تغییرات اعمال شده پس از بررسی توسط متخصصین ترینت برای کاربران قابل نمایش می باشند."
  }

}