package com.bot.services.osrm

import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Sink, Source}
import com.bot.DI.{ec, materializer, system}
import com.bot.utils.OsrmConfig
import com.typesafe.scalalogging.LazyLogging
import spray.json.JsValue

import scala.concurrent.Future

object OsrmApi extends LazyLogging {

  def getRoutes(source: (Float, Float), destination: List[(Float, Float)]): Future[(StatusCode, JsValue)] = {
    logger.info(s"""request get updates to telegram""")
    val params = destination.foldRight(s"""${source._1},${source._2}""")((x, s) => s"""$s;${x._1},${x._2}""")
    (for {
      (request, connectionFlow) <- Future {
        (HttpRequest(HttpMethods.GET, Uri(OsrmConfig.getDrivingRoutesUrl + "/" + params)
          .withQuery(Query("steps" -> "false"))
        ),
          Http().outgoingConnection(OsrmConfig.host, OsrmConfig.port, settings = OsrmConfig.clientConnectionSettings))
      }
      (status, responseBody) <- Source.single(request).via(connectionFlow).runWith(Sink.head).map(x => (x.status, x.entity))
      entity <- Unmarshal(responseBody).to[JsValue]
    } yield {
      logger.info(s"""response get updates to telegram with ${} result and status: $status""")
      (status, entity)
    }).recover {
      case error: Throwable =>
        logger.info(s"""response get updates to telegram with result:${error.getMessage} with status: ${StatusCodes.InternalServerError}""")
        (StatusCodes.InternalServerError, null)
    }
  }

  def getDurations(source: (Float, Float), destination: List[(Float, Float)]): Future[(StatusCode, JsValue)] = {
    logger.info(s"""request get table to osrm""")
    val params = destination.foldLeft(s"""${source._1},${source._2}""")((s, x) => s"""$s;${x._1},${x._2}""")
    val query = List.range(2, destination.length + 1).foldLeft("1")((s, d) => s"""$s;$d""")
    (for {
      (request, connectionFlow) <- Future {
        (HttpRequest(HttpMethods.GET, Uri(OsrmConfig.getDrivingTableUrl + "/" + params)
          .withQuery(Query("sources" -> "0", "destinations" -> query))
        ),
          Http().outgoingConnection(OsrmConfig.host, OsrmConfig.port, settings = OsrmConfig.clientConnectionSettings))
      }
      (status, responseBody) <- Source.single(request).via(connectionFlow).runWith(Sink.head).map(x => (x.status, x.entity))
      entity <- Unmarshal(responseBody).to[JsValue]
    } yield {
      logger.info(s"""response get table to osrm with ${} result and status: $status""")
      (status, entity)
    }).recover {
      case error: Throwable =>
        logger.info(s"""response get table to osrm with result:${error.getMessage} with status: ${StatusCodes.InternalServerError}""")
        (StatusCodes.InternalServerError, null)
    }
  }

}
