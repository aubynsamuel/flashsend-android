package com.aubynsamuel.flashsend.chatRoom.domain

import com.aubynsamuel.flashsend.chatRoom.data.remote.SendMessageRepository
import javax.inject.Inject

class SendImageMessageUseCase @Inject constructor(
    private val sendMessageRepository: SendMessageRepository,
    private val notificationUseCase: SendNotificationUseCase,
) {
    suspend operator fun invoke(
        caption: String,
        imageUrl: String,
        senderName: String,
        roomId: String,
        senderId: String,
        otherUserId: String,
        profileUrl: String,
        recipientsToken: String,
    ) {
        sendMessageRepository.sendImageMessage(roomId, caption, imageUrl, senderId, senderName)
        notificationUseCase(
            recipientsToken = recipientsToken,
            title = senderName,
            body = "📷 Sent an image",
            roomId = roomId,
            recipientsUserId = otherUserId,
            sendersUserId = senderId,
            profileUrl = profileUrl
        )
    }
}