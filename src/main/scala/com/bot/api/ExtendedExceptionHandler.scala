package com.bot.api

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import com.bot.formats.JsonProtocol
import com.bot.utils.{ErrorCodes, ExtendedException, HttpError, SentryClient}
import com.bot.formats.Formats._

/**
  * Custom Akka Http error handler. Courtesy of cut.social.
  */
object ExtendedExceptionHandler extends JsonProtocol {

  def handle()(implicit logger: com.typesafe.scalalogging.Logger): ExceptionHandler = {
    ExceptionHandler {
      case t@ExtendedException(content, errorCode, statusCode) =>
        SentryClient.log(t, None)
        logger.error(content, t)
        complete(HttpResponse(status = statusCode, entity = content))
      case error: Throwable =>
        SentryClient.log(error, None)
        logger.error(s"Error while handling", error)
        complete(""/*HttpError("failed", Some(ErrorCodes.GENERAL_ERROR_CODE), error.getMessage)*/)
    }

  }

}
