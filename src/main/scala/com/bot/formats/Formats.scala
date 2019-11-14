package com.bot.formats

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

import com.bot.api.HealthCheckResponse
import com.bot.api.Messages._
import com.bot.models.json._
import com.bot.models.json.bot.telegram.Message
import com.bot.models.repo.place.{Link, Logo}
import com.bot.models.repo.user.User
import com.bot.utils.HttpError
import spray.json.{RootJsonFormat, _}

object Formats extends FormatsComponent

trait FormatsComponent extends JsonProtocol {

  // models
  implicit val userInfoFormat = jsonFormat3(UserInfo)
  implicit val businessNameFormater = jsonFormat2(BusinessName)
  implicit val businessInfoFormater = jsonFormat5(BusinessInfo)
  implicit val filterUserFormater = jsonFormat5(FilterUser)
  implicit val newUserFormater = jsonFormat7(NewUser)
  implicit val editUserFormater = jsonFormat7(EditUser)
  implicit val userFormater = jsonFormat20(User)
  implicit val userLoginInfoFormat = jsonFormat2(UserLoginInfo)
  implicit val userLoginRequestFormat = jsonFormat3(UserLoginRequest)
  implicit val healthCheckResponseFormatter = jsonFormat4(HealthCheckResponse)
  implicit val httpErrorFormat = jsonFormat3(HttpError)
  implicit val smsDataFormat = jsonFormat3(SMSData)
  implicit val wMetaDataFormat = jsonFormat3(WMetaData)
  implicit val webEngageSMSBodyFormat = jsonFormat3(WebEngageSMSBody)
  implicit val nameEmailFormat = jsonFormat2(NameEmail)
  implicit val nameUrlFormat = jsonFormat2(NameUrl)
  implicit val recipientsFormat = jsonFormat3(Recipients)
  implicit val emailFormat = jsonFormat8(Email)
  implicit val webEngageEmailBodyFormat = jsonFormat3(WebEngageEmailBody)
  implicit val webEngageUserInfoFormat = jsonFormat8(WebEngageUserInfo)
  implicit val webEngageUserWithUserIdInfoFormat = jsonFormat7(WebEngageUserInfoWithUserId)
  implicit val eventUserInfoFormat = jsonFormat2(EventUserInfo)
  implicit val webEngageEventFormat = jsonFormat2(WebEngageEvent)
  // guild
  implicit val parentJsonModelFormat = jsonFormat9(guild.Parent)
  implicit val childJsonModelFormat = jsonFormat10(guild.Child)
  implicit val dataJsonModelFormat = jsonFormat10(guild.Data)
  implicit val guildJsonModelFormat = jsonFormat4(guild.Guild)
  // place
  implicit val questionJsonModelFormat = jsonFormat4(place.Question)
  implicit val gLinkJsonModelFormat = jsonFormat1(place.GLink)
  implicit val linkJsonModelFormat = jsonFormat2(place.Link)
  implicit val linkRepoModelFormat = jsonFormat2(Link)
  implicit val logoJsonModelFormat = jsonFormat2(place.Logo)
  implicit val logoRepoModelFormat = jsonFormat2(Logo)
  implicit val pGuildJsonModelFormat = jsonFormat4(place.Guild)
  implicit val rateJsonModelFormat = jsonFormat3(place.Rate)
  implicit val itemJsonModelFormat = jsonFormat14(place.Item)
  implicit val specialDataJsonModelFormat = jsonFormat2(place.SpecialData)
  implicit val generalDataJsonModelFormat = jsonFormat2(place.GeneralData)
  implicit val placeJSonModelFormat = jsonFormat3(place.Place)
  implicit val queryPlaceJSonModelFormat = jsonFormat14(com.bot.models.repo.place.QueryPlace)
  implicit val placeQueryJSonModelFormat = jsonFormat4(place.Query)
  // callback query
  implicit val botCallbackParamJSonModelFormat = jsonFormat13(com.bot.models.json.callback.Param)
  implicit val botCallbackQueryJSonModelFormat = jsonFormat2(com.bot.models.json.callback.Query)
  // bot json formatter
  implicit val botUserJSonModelFormat = jsonFormat6(com.bot.models.json.bot.telegram.User)
  implicit val botChatJSonModelFormat = jsonFormat13(com.bot.models.json.bot.telegram.Chat)
  implicit val botLocationJSonModelFormat = jsonFormat2(com.bot.models.json.bot.telegram.Location)
  implicit val osrmLegJSonModelFormat = jsonFormat5(com.bot.models.json.osrm.Leg)
  implicit val osrmRouteSonModelFormat = jsonFormat6(com.bot.models.json.osrm.Route)
  implicit val osrmGetRoutesResultJSonModelFormat = jsonFormat3(com.bot.models.json.osrm.GetRoutesResult)
  implicit val osrmGetTableResultJSonModelFormat = jsonFormat4(com.bot.models.json.osrm.GetTableResult)
  implicit val inlineQueryJSonModelFormat = jsonFormat5(com.bot.models.json.bot.telegram.InlineQuery)
  implicit val chosenInlineResultJSonModelFormat = jsonFormat5(com.bot.models.json.bot.telegram.ChosenInlineResult)
  implicit val shippingQueryJSonModelFormat = jsonFormat4(com.bot.models.json.bot.telegram.ShippingQuery)
  implicit val preCheckoutQueryJSonModelFormat = jsonFormat7(com.bot.models.json.bot.telegram.PreCheckoutQuery)
  implicit val pollOptionJSonModelFormat = jsonFormat2(com.bot.models.json.bot.telegram.PollOption)
  implicit val pollJSonModelFormat = jsonFormat4(com.bot.models.json.bot.telegram.Poll)
  implicit val callbackQueryJSonModelFormat = jsonFormat7(com.bot.models.json.bot.telegram.CallbackQuery)
  implicit val answerCallbackQueryJSonModelFormat = jsonFormat5(com.bot.models.json.bot.telegram.AnswerCallbackQuery)
  implicit val messageEntityJSonModelFormat = jsonFormat5(com.bot.models.json.bot.telegram.MessageEntity)
  implicit val contactJSonModelFormat = jsonFormat5(com.bot.models.json.bot.telegram.Contact)
  implicit val venueJSonModelFormat = jsonFormat5(com.bot.models.json.bot.telegram.Venue)
  implicit val inlineKeyboardButtonJSonModelFormat = jsonFormat8(com.bot.models.json.bot.telegram.InlineKeyboardButton)
  implicit val keyboardButtonJSonModelFormat = jsonFormat3(com.bot.models.json.bot.telegram.KeyboardButton)
  implicit val inlineKeyboardMarkupJSonModelFormat = jsonFormat1(com.bot.models.json.bot.telegram.InlineKeyboardMarkup)
  implicit val replyKeyboardMarkupJSonModelFormat = jsonFormat4(com.bot.models.json.bot.telegram.ReplyKeyboardMarkup)
  implicit val inlineQueryResultJSonModelFormat = jsonFormat0(com.bot.models.json.bot.telegram.InlineQueryResult)
  implicit val inlineQueryResultVenueJSonModelFormat = jsonFormat13(com.bot.models.json.bot.telegram.InlineQueryResultVenue)
  implicit val inputTextMessageContentJSonModelFormat = jsonFormat3(com.bot.models.json.bot.telegram.InputTextMessageContent)
  implicit val inlineQueryResultArticleJSonModelFormat = jsonFormat11(com.bot.models.json.bot.telegram.InlineQueryResultArticle)
  implicit val answerInlineQueryVenueJSonModelFormat = jsonFormat7(com.bot.models.json.bot.telegram.AnswerInlineQueryVenue)
  implicit val answerInlineQueryArticleJSonModelFormat = jsonFormat7(com.bot.models.json.bot.telegram.AnswerInlineQueryArticle)
  implicit val botSendMessageJSonModelFormat = jsonFormat7(com.bot.models.json.bot.telegram.SendMessage)
  implicit val botSendMessage1JSonModelFormat = jsonFormat7(com.bot.models.json.bot.telegram.SendMessage1)
  implicit val botSendMessage2JSonModelFormat = jsonFormat7(com.bot.models.json.bot.telegram.SendMessage2)
  implicit val botSendLocationJSonModelFormat = jsonFormat7(com.bot.models.json.bot.telegram.SendLocation)
  implicit val botSendVenueJSonModelFormat = jsonFormat10(com.bot.models.json.bot.telegram.SendVenue)
  implicit val botSendPhotoJSonModelFormat = jsonFormat7(com.bot.models.json.bot.telegram.SendPhoto)
  implicit val botSendPhoto1JSonModelFormat = jsonFormat7(com.bot.models.json.bot.telegram.SendPhoto1)
  implicit val botDeleteMessageJSonModelFormat = jsonFormat2(com.bot.models.json.bot.telegram.DeleteMessage)
  implicit val botEditMessageCaptionJSonModelFormat = jsonFormat6(com.bot.models.json.bot.telegram.EditMessageCaption)
  implicit val botUserActivityJSonModelFormat = jsonFormat10(com.bot.models.repo.user.UserActivity)
  implicit val botUserTrackingJSonModelFormat = jsonFormat8(com.bot.models.repo.user.UserTracking)
  implicit val botElasticSearchErrorJSonModelFormat = jsonFormat2(com.bot.services.telegram.TelegramBotService.Messages.ElasticSearchError)
  implicit val botElasticSearchGeoPointJSonModelFormat = jsonFormat2(com.bot.telegram.ElasticSearchActor.GeoPoint)
  implicit val botElasticUserTrackingJSonModelFormat = jsonFormat8(com.bot.telegram.ElasticSearchActor.ElasticUserTracking)
  implicit val botElasticSearchHealthJSonModelFormat = jsonFormat2(com.bot.telegram.HealthCheckActor.Health)
  implicit val queryApiJSonModelFormat = jsonFormat4(com.bot.models.json.api.Query)
  implicit val queryResultApiJSonModelFormat = jsonFormat6(com.bot.models.json.api.QueryResult)
  implicit val messageJsonFormat: RootJsonFormat[Message] = new RootJsonFormat[Message] {

    def write(message: Message) = ???

    def read(json: JsValue): Message = {
      val messageId = json.asJsObject.getFields("message_id") match {
        case Seq(JsNumber(mId)) => mId.toInt
        case unrecognized => deserializationError(s"json serialization error $unrecognized")
      }
      val from = json.asJsObject.getFields("from") match {
        case Seq(u) => Some(u.convertTo[com.bot.models.json.bot.telegram.User])
        case _ => None
      }
      val date = json.asJsObject.getFields("date") match {
        case Seq(JsNumber(d)) => d.toInt
        case unrecognized => deserializationError(s"json serialization error $unrecognized")
      }
      val chat = json.asJsObject.getFields("chat") match {
        case Seq(u) => u.convertTo[com.bot.models.json.bot.telegram.Chat]
        case unrecognized => deserializationError(s"json serialization error $unrecognized")
      }
      val t00 = json.asJsObject.getFields("forward_from") match {
        case Seq(u) => Some(u.convertTo[com.bot.models.json.bot.telegram.User])
        case _ => None
      }
      val t01 = json.asJsObject.getFields("forward_from_chat") match {
        case Seq(u) => Some(u.convertTo[com.bot.models.json.bot.telegram.Chat])
        case _ => None
      }
      val t02 = json.asJsObject.getFields("forward_from_message_id") match {
        case Seq(JsNumber(value)) => Some(value.toInt)
        case _ => None
      }
      val t03 = json.asJsObject.getFields("forward_signature") match {
        case Seq(JsString(value)) => Some(value)
        case _ => None
      }
      val t04 = json.asJsObject.getFields("forward_sender_name") match {
        case Seq(JsString(value)) => Some(value)
        case _ => None
      }
      val t05 = json.asJsObject.getFields("forward_date") match {
        case Seq(JsNumber(value)) => Some(value.toInt)
        case _ => None
      }
      val t06 = json.asJsObject.getFields("reply_to_message") match {
        case Seq(u) => Some(u)
        case _ => None
      }
      val t07 = json.asJsObject.getFields("edit_date") match {
        case Seq(JsNumber(value)) => Some(value.toInt)
        case _ => None
      }
      val t08 = json.asJsObject.getFields("media_group_id") match {
        case Seq(JsString(value)) => Some(value)
        case _ => None
      }
      val t09 = json.asJsObject.getFields("author_signature") match {
        case Seq(JsString(value)) => Some(value)
        case _ => None
      }
      val t10 = json.asJsObject.getFields("text") match {
        case Seq(JsString(value)) => Some(value)
        case _ => None
      }
      val t11 = json.asJsObject.getFields("entities") match {
        case Seq(JsArray(u)) => Some(u.map(_.convertTo[com.bot.models.json.bot.telegram.MessageEntity]).toList)
        case _ => None
      }
      val t12 = json.asJsObject.getFields("caption_entities") match {
        case Seq(JsArray(u)) => Some(u.map(_.convertTo[com.bot.models.json.bot.telegram.MessageEntity]).toList)
        case _ => None
      }
      val t13 = json.asJsObject.getFields("audio") match {
        case Seq(u) => Some(u)
        case _ => None
      }
      val t14 = json.asJsObject.getFields("document") match {
        case Seq(u) => Some(u)
        case _ => None
      }
      val t15 = json.asJsObject.getFields("animation") match {
        case Seq(u) => Some(u)
        case _ => None
      }
      val t16 = json.asJsObject.getFields("game") match {
        case Seq(u) => Some(u)
        case _ => None
      }
      val t17 = json.asJsObject.getFields("photo") match {
        case Seq(u) => Some(u)
        case _ => None
      }
      val t18 = json.asJsObject.getFields("sticker") match {
        case Seq(u) => Some(u)
        case _ => None
      }
      val t19 = json.asJsObject.getFields("video") match {
        case Seq(u) => Some(u)
        case _ => None
      }
      val t20 = json.asJsObject.getFields("voice") match {
        case Seq(u) => Some(u)
        case _ => None
      }
      val t21 = json.asJsObject.getFields("voice_note") match {
        case Seq(u) => Some(u)
        case _ => None
      }
      val t22 = json.asJsObject.getFields("caption") match {
        case Seq(u) => Some(u)
        case _ => None
      }
      val t23 = json.asJsObject.getFields("concat") match {
        case Seq(u) => Some(u.convertTo[com.bot.models.json.bot.telegram.Contact])
        case _ => None
      }
      val t24 = json.asJsObject.getFields("location") match {
        case Seq(u) => Some(u.convertTo[com.bot.models.json.bot.telegram.Location])
        case _ => None
      }
      val t25 = json.asJsObject.getFields("venue") match {
        case Seq(u) => Some(u.convertTo[com.bot.models.json.bot.telegram.Venue])
        case _ => None
      }
      val t26 = json.asJsObject.getFields("poll") match {
        case Seq(u) => Some(u.convertTo[com.bot.models.json.bot.telegram.Poll])
        case _ => None
      }
      val t27 = json.asJsObject.getFields("new_chat_members") match {
        case Seq(JsArray(u)) => Some(u.map(_.convertTo[com.bot.models.json.bot.telegram.User]).toList)
        case _ => None
      }
      val t28 = json.asJsObject.getFields("left_chat_member") match {
        case Seq(u) => Some(u.convertTo[com.bot.models.json.bot.telegram.User])
        case _ => None
      }
      val t29 = json.asJsObject.getFields("new_chat_title") match {
        case Seq(JsString(u)) => Some(u)
        case _ => None
      }
      val t30 = json.asJsObject.getFields("new_chat_photo") match {
        case Seq(u) => Some(u)
        case _ => None
      }
      val t31 = json.asJsObject.getFields("delete_chat_photo") match {
        case Seq(JsBoolean(u)) => Some(u)
        case _ => None
      }
      val t32 = json.asJsObject.getFields("group_chat_created") match {
        case Seq(JsBoolean(u)) => Some(u)
        case _ => None
      }
      val t33 = json.asJsObject.getFields("supergroup_chat_created") match {
        case Seq(JsBoolean(u)) => Some(u)
        case _ => None
      }
      val t34 = json.asJsObject.getFields("channel_chat_created") match {
        case Seq(JsBoolean(u)) => Some(u)
        case _ => None
      }
      val t35 = json.asJsObject.getFields("migrate_to_chat_id") match {
        case Seq(JsNumber(value)) => Some(value.toInt)
        case _ => None
      }
      val t36 = json.asJsObject.getFields("migrate_from_chat_id") match {
        case Seq(JsNumber(value)) => Some(value.toInt)
        case _ => None
      }
      val t37 = json.asJsObject.getFields("pinned_message") match {
        case Seq(u) => Some(u)
        case _ => None
      }
      val t38 = json.asJsObject.getFields("invoice") match {
        case Seq(u) => Some(u)
        case _ => None
      }
      val t39 = json.asJsObject.getFields("successful_payment") match {
        case Seq(u) => Some(u)
        case _ => None
      }
      val t40 = json.asJsObject.getFields("connected_website") match {
        case Seq(JsString(value)) => Some(value)
        case _ => None
      }
      val t41 = json.asJsObject.getFields("passport_data") match {
        case Seq(u) => Some(u)
        case _ => None
      }
      val t42 = json.asJsObject.getFields("reply_markup") match {
        case Seq(u) => Some(u.convertTo[com.bot.models.json.bot.telegram.InlineKeyboardMarkup])
        case _ => None
      }

      Message(
        messageId,
        from,
        date,
        chat,
        (t00, t01, t02, t03, t04, t05, t06, t07, t08, t09),
        (t10, t11, t12, t13, t14, t15, t16, t17, t18, t19),
        (t20, t21, t22, t23, t24, t25, t26, t27, t28, t29),
        (t30, t31, t32, t33, t34, t35, t36, t37, t38, t39),
        (t40, t41, t42)
      )

    }
  }
  implicit val botUpdateJSonModelFormat = jsonFormat11(com.bot.models.json.bot.telegram.Update)
  implicit val botResultJSonModelFormat = jsonFormat2(com.bot.models.json.bot.telegram.Result)
}


trait JsonProtocol extends DefaultJsonProtocol {

  val shortDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val shortDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  implicit val localDateTimeFormat: RootJsonFormat[LocalDateTime] = new RootJsonFormat[LocalDateTime] {
    def write(x: LocalDateTime) = JsString(x.format(shortDateTimeFormatter))

    def read(value: JsValue) = value match {
      case JsString(x) if x.contains("T") => LocalDateTime.parse(x, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
      case JsString(s) => LocalDateTime.parse(s, shortDateTimeFormatter)
      case x => throw new RuntimeException(s"Unexpected type ${x.getClass.getName} when trying to parse LocalDateTime")
    }
  }

  implicit val localDateFormat: RootJsonFormat[LocalDate] = new RootJsonFormat[LocalDate] {
    private val iso_date = DateTimeFormatter.ISO_DATE

    def write(x: LocalDate) = JsString(iso_date.format(x))

    def read(value: JsValue) = value match {
      case JsString(x) => LocalDate.parse(x, iso_date)
      case x => throw new RuntimeException(s"Unexpected type ${x.getClass.getName} when trying to parse LocalDateTime")
    }
  }

  implicit val uuidJsonFormat: RootJsonFormat[UUID] = new RootJsonFormat[UUID] {
    def write(uuid: UUID) = {
      uuid.toString.toJson
    }

    def read(value: JsValue) = {
      UUID.fromString(value.prettyPrint)
    }

  }

  implicit def enumFormat[T <: Enumeration](implicit enu: T): RootJsonFormat[T#Value] =
    new RootJsonFormat[T#Value] {
      def write(obj: T#Value): JsValue = JsString(obj.toString)

      def read(json: JsValue): T#Value = {
        json match {
          case JsString(txt) => enu.withName(txt)
          case somethingElse => throw DeserializationException(s"Expected a value from enum $enu instead of $somethingElse")
        }
      }
    }


}
