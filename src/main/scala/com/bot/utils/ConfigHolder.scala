package com.bot.utils

import com.bot.DI._

object ConfigHolder {
  val temp_folder: String = config.getString("csv.temp_directory")
}
