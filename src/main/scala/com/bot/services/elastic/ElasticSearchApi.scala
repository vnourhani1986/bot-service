package com.bot.services.elastic

import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Sink, Source}
import com.bot.DI.{ec, materializer, system}
import com.bot.utils.ElasticConfig
import com.typesafe.scalalogging.LazyLogging
import spray.json.JsValue

import scala.concurrent.Future

object ElasticSearchApi extends LazyLogging {

  def post(message: JsValue, url: String): Future[(StatusCode, JsValue)] = {
    
    (for {
      body <- Marshal(message).to[RequestEntity]
      (request, connectionFlow) <- Future {
        (HttpRequest(HttpMethods.POST, url)
          .withEntity(body.withContentType(ContentTypes.`application/json`)
          ),
          Http().outgoingConnection(host = ElasticConfig.host, port = ElasticConfig.port, settings = ElasticConfig.clientConnectionSettings))
      }
      (status, responseBody) <- Source.single(request).via(connectionFlow).runWith(Sink.head).map(x => (x.status, x.entity))
      entity <- Unmarshal(responseBody).to[JsValue]
    } yield {
      (status, entity)
    }).recover {
      case error: Throwable =>
        logger.info(s"""response post index to elastic search with result:${error.getMessage} with status: ${StatusCodes.InternalServerError}""")
        (StatusCodes.InternalServerError, null)
    }
  }

}