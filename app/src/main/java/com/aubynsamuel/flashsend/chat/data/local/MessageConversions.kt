package com.aubynsamuel.flashsend.chat.data.local

import com.aubynsamuel.flashsend.chat.data.model.MessageEntity
import com.aubynsamuel.flashsend.core.domain.model.ChatMessage

fun ChatMessage.toMessageEntity(roomId: String): MessageEntity {
    return MessageEntity(
        id = id,
        content = content,
        image = image,
        audio = audio,
        createdAt = createdAt,
        senderId = senderId,
        senderName = senderName,
        replyTo = replyTo,
        read = read,
        type = type,
        delivered = delivered,
        location = location,
        duration = duration,
        roomId = roomId,
        reactions = reactions
    )
}

fun MessageEntity.toChatMessage(): ChatMessage {
    return ChatMessage(
        id = id,
        content = content,
        image = image,
        audio = audio,
        createdAt = createdAt,
        senderId = senderId,
        senderName = senderName,
        replyTo = replyTo,
        read = read,
        type = type,
        delivered = delivered,
        location = location,
        duration = duration,
        reactions = reactions.toMutableMap()
    )
}