package com.bot.utils

import com.bot.DI._
import com.bot.models.repo.review.QuestionSubGuild
import com.bot.repos.review.QuestionSubGuildRepoImpl
import com.bot.utils.PostgresProfiler.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object QuestionUtil {

  def updateQuestionSubGuildTable(): Unit = {

    val query = sql"""SELECT DISTINCT unnest(child_guild_ids) as sub_guild_id, unnest(question_ids) as question_id from place join user_rate on uuid = place_uuid ORDER BY sub_guild_id;"""
      .as[(Long, Long)]

    db.run(query).map(_.map { qsg =>
      println(qsg._1)
      val result = QuestionSubGuildRepoImpl.save(QuestionSubGuild(
        subGuildId = qsg._1,
        questionId = qsg._2,
        createdAt = DateTimeUtils.now
      ))
      Await.result(result, Duration.Inf)
    })

  }

}


//object runner extends App {
//  QuestionUtil.updateQuestionSubGuildTable()
//}
