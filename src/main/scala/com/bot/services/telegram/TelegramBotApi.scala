package com.bot.services.telegram

import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Sink, Source}
import com.bot.DI.{ec, materializer, system}
import com.bot.services.telegram.TelegramBotService.Messages.{ElasticSearchError, SendErrorToElasticSearchActor}
import com.bot.services.telegram.TelegramBotService.elasticSearchActor
import com.bot.utils.{DateTimeUtils, TelegramBotConfig}
import com.typesafe.scalalogging.LazyLogging
import spray.json.{JsValue, _}
import com.bot.formats.Formats._

import scala.concurrent.Future

object TelegramBotApi extends LazyLogging {

  def getUpdates(offset: Int, limit: Int): Future[(StatusCode, JsValue)] = {

    (for {
      (request, connectionFlow) <- Future {
        (HttpRequest(HttpMethods.GET, Uri(TelegramBotConfig.getUpdatesUrl)
          .withQuery(Query("offset" -> offset.toString, "limit" -> limit.toString))
        ),
          Http().outgoingConnectionHttps(TelegramBotConfig.host, settings = TelegramBotConfig.clientConnectionSettings))
      }
      (status, responseBody) <- Source.single(request).via(connectionFlow).runWith(Sink.head).map(x => (x.status, x.entity))
      entity <- Unmarshal(responseBody).to[JsValue]
    } yield {
      (status, entity)
    }).recover {
      case error: Throwable =>
        logger.info(s"""response get updates to telegram with result:${error.getMessage} with status: ${StatusCodes.InternalServerError}""")
        elasticSearchActor ! SendErrorToElasticSearchActor(
          ElasticSearchError(
            s"""response get updates to telegram with result:${error.getMessage} with status: ${StatusCodes.InternalServerError}""", DateTimeUtils.now).toJson,
          s"""/error_idx/error""")
        (StatusCodes.InternalServerError, null)
    }
  }

  def send(message: JsValue, url: String): Future[(StatusCode, JsValue)] = {

    (for {
      body <- Marshal(message).to[RequestEntity]
      (request, connectionFlow) <- Future {
        (HttpRequest(HttpMethods.POST, url)
          .withEntity(body.withContentType(ContentTypes.`application/json`)
          ),
          Http().outgoingConnectionHttps(TelegramBotConfig.host, settings = TelegramBotConfig.clientConnectionSettings))
      }
      (status, responseBody) <- Source.single(request).via(connectionFlow).runWith(Sink.head).map(x => (x.status, x.entity))
      entity <- Unmarshal(responseBody).to[JsValue]
    } yield {
      (status, entity)
    }).recover {
      case error: Throwable =>
        logger.info(s"""response send message to telegram with result:${error.getMessage} with status: ${StatusCodes.InternalServerError}""")
        elasticSearchActor ! SendErrorToElasticSearchActor(
          ElasticSearchError(
            s"""response send message to telegram with result:${error.getMessage} with status: ${StatusCodes.InternalServerError}""", DateTimeUtils.now).toJson,
          s"""/error_idx/error""")
        (StatusCodes.InternalServerError, null)
    }
  }

}