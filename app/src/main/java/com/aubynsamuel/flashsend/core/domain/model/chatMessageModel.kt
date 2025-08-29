package com.aubynsamuel.flashsend.core.domain.model

import androidx.annotation.Keep
import java.util.Date

@Keep
data class ChatMessage(
    var id: String = "",
    var content: String = "",
    var image: String? = null,
    var audio: String? = null,
    var createdAt: Date = Date(),
    var senderId: String = "",
    var senderName: String = "",
    var replyTo: String? = null,
    var read: Boolean = false,
    var type: String = "text",
    var delivered: Boolean = false,
    var location: Location? = null,
    var duration: Long? = null,
    var reactions: MutableMap<String, String> = mutableMapOf(),
)

data class Location(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
)