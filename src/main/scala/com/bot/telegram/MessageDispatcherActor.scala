package com.bot.telegram

import akka.actor.{Actor, ActorSystem}
import akka.util.Timeout
import com.bot.models.json.bot.telegram.{Chat, InlineKeyboardMarkup, User}
import com.bot.services.telegram.TelegramBotService.Messages._
import com.bot.services.telegram.TelegramBotService.{Messages, _}
import com.bot.telegram.MessageDispatcherActor._
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext

class MessageDispatcherActor(
                              implicit
                              system: ActorSystem,
                              ec: ExecutionContext,
                              timeout: Timeout
                            ) extends Actor with LazyLogging {

  self ! Start()

  override def receive(): Receive = {

    case Start() =>
      logger.info(s"""message dispatcher actor is started""")

    case SendMessageToDispatcher(message) =>

      val messageId = message.message_id
      val chat = message.chat
      val user = message.from
      val text = message.p1._1
      val entities = message.p1._2
      text.foreach { t =>
        entities match {
          case Some(es) =>
            es.foreach { e =>
              e.`type` match {
                case "bot_command" => userActor ! SendCommandToUserActor(chat, user, t)
                case "username" => userActor ! SendUsernameToUserActor(chat, user, t)
                case "phone_number" => userActor ! SendTextToUserActor(chat, user, t)
                case "text_link" => userActor ! SendTextToUserActor(chat, user, t)
                case "url" => userActor ! SendTextToUserActor(chat, user, t)
                case _ =>
              }
            }
          case None =>
            userActor ! SendTextToUserActor(chat, user, t)
        }
      }

      val location = message.p2._5
      location.foreach(l => self ! Location(messageId, chat, user, l))

      val replyMarkup = message.p4._3
      replyMarkup.foreach(r => self ! ReplyMarkup(r))

    case Location(messageId, chat, user, location) =>

      userActor ! SendLocationToUserActor(messageId, chat, user, location)

    case ReplyMarkup(replyMarkup) =>
      logger.info(s"""reply markup $replyMarkup to message dispatcher actor""")

    case _ =>
      logger.info(s"""welcome to message dispatcher actor""")

  }

}

object MessageDispatcherActor {

  case class Location(messageId: Int, chat: Chat, user: Option[User], location: com.bot.models.json.bot.telegram.Location)

  case class ReplyMarkup(reply: InlineKeyboardMarkup)

}