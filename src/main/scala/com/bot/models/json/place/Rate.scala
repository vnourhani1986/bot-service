package com.bot.models.json.place

case class Rate(
                 average: Option[Int] = None,
                 count: Option[Int] = None,
                 questions: List[Question] = Nil
               )


