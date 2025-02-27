package com.aubynsamuel.flashsend.functions

import com.aubynsamuel.flashsend.chatRoom.MessageEntity

// Extension functions for conversions with logging.
fun ChatMessage.toMessageEntity(roomId: String): MessageEntity {
//    Log.d(
//        "ChatViewModel",
//        "Converting ChatMessage (id=${this.id}) to MessageEntity with roomId=$roomId"
//    )
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
//    Log.d("ChatViewModel", "Converting MessageEntity (id=${this.id}) to ChatMessage")
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
        reactions = reactions
    )
}