package com.bot.models.json.guild


case class Data(
                 id: Option[Long] = None,
                 player_id: Option[Long] = None,
                 title: Option[String] = None,
                 parent_id: Option[Long] = None,
                 public: Option[Int] = None,
                 icon: Option[String] = None,
                 version: Option[Int] = None,
                 template: Option[String] = None, // todo ???
                 equality: Option[Int] = None,
                 children: List[Child] = Nil
               )
