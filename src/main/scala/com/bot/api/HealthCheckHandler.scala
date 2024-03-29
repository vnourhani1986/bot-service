package com.bot.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.bot.DI._
import com.bot.formats.Formats._

import com.bot.utils.PostgresProfiler.api._

import scala.concurrent.Future

/**
  * Handle requests to monitor healthcheck
  */

case class HealthCheckResponse(result: String, summery: String, remoteAddress: String, db: String)

object HealthCheckHandler {

  private def handle(ctx: RequestContext): Future[RouteResult] = {
    val remoteAddress = ctx.request.headers.find(h => "Remote-Address".equalsIgnoreCase(h.name)).getOrElse(new RawHeader("Remote-Address", "127.0.0.1")).value()

    db.run(sql"""SELECT 1""".as[Boolean]) flatMap { _ =>
      val h = HealthCheckResponse("success", "This is from a healthy market service app :D", remoteAddress, "successfully ran a `select 1` query")
      ctx.complete(h)
    } recoverWith {
      case error: Throwable => ctx.fail(error)
    }
  }

  // Route to this handler
  val route: Route =
    (get & pathPrefix("health")) { ctx =>
      handle(ctx)
    }
}