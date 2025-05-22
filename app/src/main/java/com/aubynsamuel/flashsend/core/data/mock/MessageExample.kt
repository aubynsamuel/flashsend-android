package com.aubynsamuel.flashsend.core.data.mock

import com.aubynsamuel.flashsend.core.model.ChatMessage
import java.util.Date

val messageExample: ChatMessage = ChatMessage(
    id = "12",
    content = "Sample message preview",
    createdAt = Date(),
    senderId = "",
    senderName = "",
    read = true,
    delivered = true,
    type = "text",
    duration = 290
)