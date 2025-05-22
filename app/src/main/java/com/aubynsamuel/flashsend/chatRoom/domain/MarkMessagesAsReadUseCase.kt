package com.aubynsamuel.flashsend.chatRoom.domain

import com.aubynsamuel.flashsend.chatRoom.data.remote.MessageRepository
import com.aubynsamuel.flashsend.core.model.ChatMessage
import javax.inject.Inject

class MarkMessagesAsReadUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
) {
    suspend operator fun invoke(roomId: String, userId: String, messages: List<ChatMessage>) {
        messageRepository.markMessagesAsRead(roomId, userId, messages)
    }
}
