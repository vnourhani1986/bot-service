package com.bot.api

import akka.actor._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpResponse, StatusCodes, _}
import akka.http.scaladsl.server.Directives.{entity, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.bot.formats.Formats._
import com.bot.models.json.api.{Query, QueryResult}
import com.bot.services.telegram.TelegramBotService.Messages._
import com.bot.services.telegram.TelegramBotService.findPlaceActor
import com.typesafe.scalalogging.LazyLogging
import spray.json._

import scala.concurrent.ExecutionContextExecutor

class RouteHandler(system: ActorSystem, timeout: Timeout) extends LazyLogging {

  implicit val requestTimeout: Timeout = timeout

  implicit def executionContext: ExecutionContextExecutor = system.dispatcher

  def routs: Route = HealthCheckHandler.route ~ api

  def api: Route = {
    pathPrefix("api" / "v1") {
      path("query") {
        post {
          headerValue(extractHeader("x-auth-id")) { _ =>
            entity(as[Query]) { body =>
              onSuccess(findPlaceActor.ask(SendApiQueryToFindPlaceActor(body.query, body.lat, body.lng, body.page)).mapTo[Seq[QueryResult]]) {
                entity =>
                  val httpEntity = HttpEntity(ContentTypes.`application/json`, entity.toJson.compactPrint)
                  complete(HttpResponse(status = StatusCodes.OK).withEntity(httpEntity))
              }
            }
          }
        }
      }
    }
  }

  def extractHeader(attr: String): HttpHeader => Option[String] = {
    case HttpHeader(`attr`, value) => Some(value)
    case _ => None
  }

}
