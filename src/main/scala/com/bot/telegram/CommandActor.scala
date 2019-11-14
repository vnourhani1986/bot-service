package com.bot.telegram

import akka.actor.{Actor, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.bot.models.json.bot.telegram.Chat
import com.bot.models.repo.guild.Guild
import com.bot.services.telegram.TelegramBotService.Messages._
import com.bot.services.telegram.TelegramBotService.{callbackDispatcherActor, editPlaceActor, postgresActor}
import com.bot.telegram.CommandActor._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext

class CommandActor(
                    implicit
                    system: ActorSystem,
                    ec: ExecutionContext,
                    timeout: Timeout
                  ) extends Actor with LazyLogging {

  self ! Start()

  override def receive(): Receive = {

    case Start() =>
      logger.info(s"""command actor is started""")

    case SendCommandToCommandActor(chat, command) =>

      if (command == Commands.START) {
        self ! StartCommand(chat.id)
      } else if (command == Commands.FIND_PLACE) {
        self ! FindPlaceCommand(chat)
      } else if (command == Commands.ADD_PLACE) {
        self ! AddPlace(chat)
      } else if (command == Commands.EDIT_PLACE) {
        self ! EditPlace(chat)
      } else if (command == Commands.EDIT_PLACE_TITLE) {
        self ! EditPlaceTitle(chat)
      } else if (command == Commands.EDIT_PLACE_ADDRESS) {
        self ! EditPlaceAddress(chat)
      } else if (command == Commands.EDIT_PLACE_PHONE) {
        self ! EditPlacePhone(chat)
      } else if (command == Commands.EDIT_PLACE_LOCATION) {
        self ! EditPlaceLocation(chat)
      } else if (command == Commands.EDIT_PLACE_CITY_TITLE) {
        self ! EditPlaceCity(chat)
      } else if (command == Commands.EDIT_PLACE_SUB_GUILD_TITLE) {
        self ! EditPlaceSubGuild(chat)
      } else if (command == Commands.EDIT_PLACE_LINK) {
        self ! EditPlaceLink(chat)
      }

    case StartCommand(chatId) =>

      callbackDispatcherActor ! CreateMainMenu(chatId)
    //
    //      senderActor ! SendPhoto(chat_id = chatId,
    //        photo = TelegramBotConfig.startCommandPhoto,
    //        caption = Some(s"""${CommandActor.Contents.GO_TO_VIEW_COMMANDS_1}
    //           |<a>${Commands.FIND_PLACE}</a>
    //           |${CommandActor.Contents.GO_TO_VIEW_COMMANDS_2}
    //           |@${TelegramBotConfig.name} ${CommandActor.Contents.GO_TO_VIEW_COMMANDS_3}
    //           |${CommandActor.Contents.GO_TO_VIEW_COMMANDS_4}
    //           |<a>${Commands.ADD_PLACE}</a>
    //           |${CommandActor.Contents.GO_TO_VIEW_COMMANDS_5}
    //           |<a>${Commands.EDIT_PLACE}</a>
    //           |${CommandActor.Contents.GO_TO_VIEW_COMMANDS_6}
    //           |""".stripMargin),
    //        parse_mode = Some("HTML"))

    case FindPlaceCommand(chat) =>

      for {
        guilds <- (postgresActor ? GetGuilds()).mapTo[Seq[Guild]]
      } yield {
        callbackDispatcherActor ! SendGuildsToCallbackDispatcher(chat, guilds)
      }

    case AddPlace(chat) =>

    //      addPlaceRouterActor ! SendAddPlaceToAddPlaceRouterActor(chat)

    case EditPlace(chat) =>

    //      editPlaceActor ! SendEditPlaceToEditPlaceActor(chat)

    case EditPlaceTitle(chat) =>

      editPlaceActor ! SendEditPlaceTitleToEditPlaceActor(chat)

    case EditPlaceAddress(chat) =>

      editPlaceActor ! SendEditPlaceAddressToEditPlaceActor(chat)

    case EditPlacePhone(chat) =>

      editPlaceActor ! SendEditPlacePhoneToEditPlaceActor(chat)

    case EditPlaceLocation(chat) =>

      editPlaceActor ! SendEditPlaceLocationToEditPlaceActor(chat)

    case EditPlaceCity(chat) =>

      editPlaceActor ! SendEditPlaceCityToEditPlaceActor(chat)

    case EditPlaceSubGuild(chat) =>

      editPlaceActor ! SendEditPlaceSubGuildToEditPlaceActor(chat)

    case EditPlaceLink(chat) =>

      editPlaceActor ! SendEditPlaceLinkToEditPlaceActor(chat)

    case _ =>
      logger.info(s"""welcome to command actor""")

  }

}

object CommandActor {

  object Commands {
    val START = "/start"
    val FIND_PLACE = "/findplace"
    val ADD_PLACE = "/addplace"
    val EDIT_PLACE = "/editplace"
    // edit place
    val EDIT_PLACE_TITLE = "/title"
    val EDIT_PLACE_ADDRESS = "/address"
    val EDIT_PLACE_PHONE = "/phone"
    val EDIT_PLACE_LOCATION = "/location"
    val EDIT_PLACE_CITY_TITLE = "/city"
    val EDIT_PLACE_SUB_GUILD_TITLE = "/guild"
    val EDIT_PLACE_LINK = "/link"
  }

  object Contents {
    val GO_TO_VIEW_COMMANDS_1 = "به سرویس مکان یابی ترینت خوش آمدید. لطفا با کلیک بر روی هر یک از دستورات وارد بخش مربوطه شوید."
    val GO_TO_VIEW_COMMANDS_2 = "توسط این دستور می توانید وارد بخش اصلی سرویس یعنی یافتن مکان های سراسر ایران شوید. استفاده از این دستور یکی از دو روشی است که سرویس ترینت جهت مکان یابی استفاده می کند. روش دیگر مکان یابی در ترینت استفاده از موتور جستجوی ترینت می باشد که می توانید به مکان های مورد نظر خود از طریق وارد کردن نام آنها یا کلمات کلیدی مرتبط دسترسی پیدا کنید. به عنوان مثال جهت یافتن قنادی لادن می توانید دستور زیر را در قسمت ورودی متن وارد کنید."
    val GO_TO_VIEW_COMMANDS_3 = "قنادی لادن"
    val GO_TO_VIEW_COMMANDS_4 = "استفاده از این روش نیازی به قرار داشتن در چت بات نیست و می توانید در چت های خصوصی و یا گروه ها و کانال ها از آن استفاده کنید."
    val GO_TO_VIEW_COMMANDS_5 = "توسط این دستور می توانید مکان جدیدی به سرویس ترینت اضافه کنید."
    val GO_TO_VIEW_COMMANDS_6 = "توسط این دستور می توانید مکان ثبت شده خود را ویرایش نمایید."
  }

  case class FindPlaceCommand(chat: Chat)

  case class AddPlace(chat: Chat)

  case class EditPlace(chat: Chat)

  case class EditPlaceTitle(chat: Chat)

  case class EditPlaceAddress(chat: Chat)

  case class EditPlacePhone(chat: Chat)

  case class EditPlaceLocation(chat: Chat)

  case class EditPlaceCity(chat: Chat)

  case class EditPlaceSubGuild(chat: Chat)

  case class EditPlaceLink(chat: Chat)

}
