package com.bot.models.json.bot.telegram

case class ReplyKeyboardMarkup(
                                keyboard: List[List[KeyboardButton]],
                                resize_keyboard: Option[Boolean] = None,
                                one_time_keyboard: Option[Boolean] = None,
                                selective: Option[Boolean] = None
                              )
