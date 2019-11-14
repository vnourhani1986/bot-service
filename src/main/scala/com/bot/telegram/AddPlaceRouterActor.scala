package com.bot.telegram

import akka.actor.{Actor, ActorSystem, Props}
import akka.util.Timeout
import com.bot.services.telegram.TelegramBotService.Messages._
import com.bot.services.telegram.TelegramBotService.callbackDispatcherActor
import com.bot.telegram.AddPlaceActor.Contents
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext

class AddPlaceRouterActor(
                           implicit
                           system: ActorSystem,
                           ec: ExecutionContext,
                           timeout: Timeout
                         ) extends Actor with LazyLogging {

  self ! Start()

  override def receive(): Receive = {

    case Start() =>
      logger.info(s"""add place router actor is started""")

    case SendAddPlaceToAddPlaceRouterActor(user) =>

      callbackDispatcherActor ! SendAddPlaceWelcomeToCallbackDispatcher(user, Contents.ADD_PLACE_TITLE, Contents.START)

    case SendStartAddPlaceToAddPlaceRouterActor(user) =>

      context.actorOf(Props(new AddPlaceActor(user.id)), s"add-place-actor-${user.id}")

    case message: SendTextToAddPlaceActor =>

      context.child(s"add-place-actor-${message.user.map(_.id).getOrElse(message.chat.id)}").foreach(_.forward(message))

    case message: SendLocationToAddPlaceActor =>

      context.child(s"add-place-actor-${message.user.map(_.id).getOrElse(message.chat.id)}").foreach(_.forward(message))

    case message: SendInlineCityResult =>

      context.child(s"add-place-actor-${message.chosenInlineResult.from.id}").foreach(_.forward(message))

    case message: SendInlineSubGuildResult =>

      context.child(s"add-place-actor-${message.chosenInlineResult.from.id}").foreach(_.forward(message))

    case _ =>
      logger.info(s"""welcome to add place router actor""")

  }

}
