package com.bot.telegram

import akka.actor.{Actor, ActorSystem, Cancellable, PoisonPill}
import akka.pattern.ask
import akka.util.Timeout
import com.bot.models.json.bot.telegram._
import com.bot.models.repo.user.UserActivity
import com.bot.services.telegram.TelegramBotService.Messages._
import com.bot.services.telegram.TelegramBotService.{callbackDispatcherActor, postgresActor, senderActor, userActor}
import com.bot.telegram.AddPlaceActor._
import com.bot.telegram.UserActor.{Action, SaveUserActivityWithUserInfo}
import com.bot.utils.TelegramBotConfig
import com.bot.utils.formatters.MobileNoFormatter._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class AddPlaceActor(userId: Int)(
  implicit
  system: ActorSystem,
  ec: ExecutionContext,
  timeout: Timeout
) extends Actor with LazyLogging {

  var cityId: Long = _
  var subGuildId: Long = _
  var title: String = _
  var address: String = _
  var phone: String = _
  var location: Location = _
  var link: Option[String] = _

  self ! Start()

  override def receive(): Receive = {

    case Start() =>
      logger.info(s"""add place actor is started""")
      senderActor ! SendMessage(userId, Contents.ADD_PLACE_ASK_TITLE)
      killer(10.minute)

    case SendTextToAddPlaceActor(chat, user, text, lastAction) =>

      lastAction match {
        case Action.START_ADD_PLACE => self ! SetTitle(chat, user, text)
        case Action.SET_PLACE_TITLE => self ! SetAddress(chat, user, text)
        case Action.SET_PLACE_ADDRESS => self ! SetPhone(chat, user, text)
        case Action.SET_PLACE_SUB_GUILD_ID => self ! SetLink(chat, user, text)
      }

    case SendLocationToAddPlaceActor(chat, user, loc) =>

      (userActor ? SaveUserActivityWithUserInfo(user.get, chat, Action.SET_PLACE_LOCATION, None)).mapTo[UserActivity].map { _ =>
        location = loc
        senderActor ! SendMessage(chat.id, Contents.ADD_PLACE_ASK_CITY_TITLE)
      }

    case SetTitle(chat, user, text) =>

      (userActor ? SaveUserActivityWithUserInfo(user.get, chat, Action.SET_PLACE_TITLE, None)).mapTo[UserActivity].map { _ =>
        title = text
        senderActor ! SendMessage(chat.id, Contents.ADD_PLACE_ASK_ADDRESS)
      }

    case SetAddress(chat, user, text) =>

      (userActor ? SaveUserActivityWithUserInfo(user.get, chat, Action.SET_PLACE_ADDRESS, None)).mapTo[UserActivity].map { _ =>
        address = text
        senderActor ! SendMessage(chat.id, Contents.ADD_PLACE_ASK_PHONE)
      }


    case SetPhone(chat, user, text) =>

      phone = format(Some(text)).getOrElse("")
      if (phone.length == IRAN_PHONE_LENGTH - 1) {
        senderActor ! SendMessage(chat.id,
          s"""${Contents.ADD_PLACE_ASK_LOCATION_1}
             |${Contents.ADD_PLACE_ASK_LOCATION_2}
             |${Contents.ADD_PLACE_ASK_LOCATION_3}
             |<a>${Contents.ADD_PLACE_ASK_LOCATION_4}</a>
             |${Contents.ADD_PLACE_ASK_LOCATION_5}
             |<a>${Contents.ADD_PLACE_ASK_LOCATION_6}</a>
             |${Contents.ADD_PLACE_ASK_LOCATION_7}
           """.stripMargin,
          parse_mode = Some("HTML"))
        (userActor ? SaveUserActivityWithUserInfo(user.get, chat, Action.SET_PLACE_PHONE, None)).mapTo[UserActivity]
      } else {
        callbackDispatcherActor ! CreateBackToMainMenu(userId, TelegramBotConfig.failPhoto, Contents.ADD_PLACE_ERROR)
      }

    case SendInlineCityResult(chosenInlineResult) =>

      cityId = chosenInlineResult.result_id.toLong
      senderActor ! SendMessage(chosenInlineResult.from.id, Contents.ADD_PLACE_ASK_SUB_GUILD_TITLE)


    case SendInlineSubGuildResult(chosenInlineResult) =>

      subGuildId = chosenInlineResult.result_id.toLong
      senderActor ! SendMessage(chosenInlineResult.from.id, Contents.ADD_PLACE_ASK_LINK)


    case SetLink(chat, user, text) =>

      for {
        _ <- (userActor ? SaveUserActivityWithUserInfo(user.get, chat, Action.SET_PLACE_LINK, None)).mapTo[UserActivity]
        _ = link = if (text == Contents.ADD_PLACE_NO_LINK) None else Some(text)
        _ <- (postgresActor ? AddPlaceToPostgresActor(user.map(_.id.toLong).getOrElse(cityId), cityId, subGuildId, title, address, phone, location.latitude, location.longitude, link)).mapTo[(Option[Long], Option[Long], Option[Long])]
      } yield {
        callbackDispatcherActor ! CreateBackToMainMenu(userId, TelegramBotConfig.successPhoto, Contents.ADD_PLACE_FINISH)
        self ! PoisonPill
      }

    case KillMe() =>

      self ! PoisonPill

    case _ =>
      logger.info(s"""welcome to add place actor""")

  }

  def killer(time: FiniteDuration): Cancellable = {
    context.system.scheduler.scheduleOnce(time, self, KillMe())
  }

}

object AddPlaceActor {

  case class SetTitle(chat: Chat, user: Option[User], title: String)

  case class SetAddress(chat: Chat, user: Option[User], address: String)

  case class SetPhone(chat: Chat, user: Option[User], phone: String)

  case class SetLink(chat: Chat, user: Option[User], link: String)

  case class KillMe()

  object Contents {
    val ADD_PLACE_TITLE = "این بخش امکان اضافه کردن کسب و کار شما در سرویس ترینت را فراهم می کند. پس از ثبت مکان توسط شما در ترینت و بررسی متخصصین ترینت کسب و کار شما به تمامی کاربران نمایش داده می شود. با کلیک بر روی دکمه زیر ثبت مکان کسب و کار خود را آغاز کنید."
    val START = "شروع"
    val ADD_PLACE_ASK_CITY_TITLE = "لطفا نام شهری را که کسب و کار در آن قرار دارد وارد نمایید. جهت انجام این کار می بایست نام بات یعنی @my3netbot را در قسمت ورودی متن وارد کرده و در مقابل آن نام شهر محل کسب و کار را جستجو کنید. در نهایت در لیست نمایش داده شده شهر کسب و کار خود را انتخاب نمایید."
    val ADD_PLACE_ASK_SUB_GUILD_TITLE = "لطفا نام صنفی که کسب و کار زیر مجموعه آن است را وارد نمایید. جهت انجام این کار می بایست نام بات یعنی @my3netbot را در قسمت ورودی متن وارد کرده و در مقابل آن نام صنف مربوط به کسب و کار را جستجو کنید. در نهایت در لیست نمایش داده شده صنف کسب و کار خود را انتخاب نمایید."
    val ADD_PLACE_ASK_TITLE = "لطفا نام کسب و کاری که قصد ثبت آن را دارید مشخص نمایید."
    val ADD_PLACE_ASK_ADDRESS = "لطفا آدرس محل کسب و کاری که قصد ثبت آن را دارید مشخص نمایید."
    val ADD_PLACE_ASK_PHONE = "لطفا شماره تلفن کسب و کاری که قصد ثبت آن را دارید مشخص نمایید."
    val ADD_PLACE_ASK_LOCATION_1 = "لطفا لوکیشن محل کسب و کاری که قصد ثبت آن را دارید مشخص نمایید. دو روش جهت دریافت لوکیشن وجود دارد."
    val ADD_PLACE_ASK_LOCATION_2 = "۱) انتخاب لوکیشن از طریق دکمه پایین صفحه: این روش تنها قابلیت ارسال لوکیشن فعلی شما به سرویس ترینت را دارد."
    val ADD_PLACE_ASK_LOCATION_3 = "۲) استفاده از روش معمول در تلگرام: جهت آشنایی با چگونگی استفاده از این روش در گوشی های اندروید از لینک زیر"
    val ADD_PLACE_ASK_LOCATION_4 = "https://www.youtube.com/watch?v=6lQVkQGm4aU"
    val ADD_PLACE_ASK_LOCATION_5 = "و در گوشی های آی او اس از لینک زیر کمک بگیرید."
    val ADD_PLACE_ASK_LOCATION_6 = "https://www.youtube.com/watch?v=e270rWBCYoc"
    val ADD_PLACE_ASK_LOCATION_7 = "حال یکی از دو روش بالا را انتخاب نمایید تا لوکیشن محل کسب و کار ثبت شود."
    val ADD_PLACE_ASK_LINK = "لطفا در صورتی که برای کسب و کار خود وبسایت دارید لینک آن را وارد نمایید. در صورت نداشتن وب سایت کلمه ندارم را وارد نمایید."
    val ADD_PLACE_FINISH = "از این که مکان کسب و کار خود را در ترینت به اشتراک گذاشتید سپاسگزاریم. مکان کسب و کار ثبت شده پس از بررسی توسط متخصصین ترینت برای کاربران قابل نمایش خواهد بود."
    val ADD_PLACE_ERROR = "اطلاعات وارد شده صحیح نمی باشند."
    val ADD_PLACE_NO_LINK = "ندارم"
  }

  val IRAN_PHONE_LENGTH = 11

}