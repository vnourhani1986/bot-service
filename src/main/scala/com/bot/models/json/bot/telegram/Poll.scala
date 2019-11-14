package com.bot.models.json.bot.telegram

case class Poll(
                 id: String,
                 question: String,
                 options: List[PollOption],
                 is_closed: Boolean
               )

