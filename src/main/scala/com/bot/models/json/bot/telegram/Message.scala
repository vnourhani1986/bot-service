package com.bot.models.json.bot.telegram

import spray.json.JsValue

case class Message(
                    message_id: Int,
                    from: Option[User],
                    date: Int,
                    chat: Chat,
                    p0: (
                      Option[User], // forward_from
                        Option[Chat], // forward_from_chat
                        Option[Int], // forward_from_message_id
                        Option[String], // forward_signature
                        Option[String], // forward_sender_name
                        Option[Int], // forward_date
                        Option[JsValue], // reply_to_message
                        Option[Int], // edit_date
                        Option[String], // media_group_id
                        Option[String] // author_signature
                      ),
                    p1: (
                      Option[String], // text
                        Option[List[MessageEntity]], // entities
                        Option[List[MessageEntity]], // caption_entities
                        Option[JsValue], // audio todo: audio object
                        Option[JsValue], // document todo: document object
                        Option[JsValue], // animation todo: animation object
                        Option[JsValue], // game todo: game object
                        Option[JsValue], // photo todo: photo
                        Option[JsValue], // sticker todo: sticker
                        Option[JsValue] // video todo: video
                      ),
                    p2: (
                      Option[JsValue], // voice todo: voice
                        Option[JsValue], // video_note todo: video_note
                        Option[JsValue], // caption todo: caption
                        Option[Contact], // contact
                        Option[Location], // location
                        Option[Venue], // venue
                        Option[Poll], // poll
                        Option[List[User]], // new_chat_members
                        Option[User], // left_chat_member
                        Option[String] // new_chat_title
                      ),
                    p3: (
                      Option[JsValue], // new_chat_photo todo:  photo size object
                        Option[Boolean], // delete_chat_photo
                        Option[Boolean], // group_chat_created
                        Option[Boolean], // supergroup_chat_created
                        Option[Boolean], // channel_chat_created
                        Option[Int], // migrate_to_chat_id
                        Option[Int], // migrate_from_chat_id
                        Option[JsValue], // pinned_message
                        Option[JsValue], // invoice todo:  invoice
                        Option[JsValue] // successful_payment todo:  successful_payment
                      ),
                    p4: (
                      Option[String], // connected_website
                        Option[JsValue], // passport_data todo:  passport_data
                        Option[InlineKeyboardMarkup] // reply_markup
                      )
                  )
