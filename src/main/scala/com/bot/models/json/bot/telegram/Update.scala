package com.bot.models.json.bot.telegram

case class Update(
                   update_id: Int,
                   message: Option[Message],
                   edited_message: Option[Message],
                   channel_post: Option[Message],
                   edited_channel_post: Option[Message],
                   inline_query: Option[InlineQuery],
                   chosen_inline_result: Option[ChosenInlineResult],
                   callback_query: Option[CallbackQuery],
                   shipping_query: Option[ShippingQuery],
                   pre_checkout_query: Option[PreCheckoutQuery],
                   poll: Option[Poll]
                 )

