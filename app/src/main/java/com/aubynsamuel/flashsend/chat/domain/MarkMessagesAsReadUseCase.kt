package com.aubynsamuel.flashsend.chat.domain

import com.aubynsamuel.flashsend.chat.data.repository.MessageRepository
import com.aubynsamuel.flashsend.core.domain.model.ChatMessage
import javax.inject.Inject

class MarkMessagesAsReadUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
) {
    suspend operator fun invoke(roomId: String, userId: String, messages: List<ChatMessage>) {
        messageRepository.markMessagesAsRead(roomId, userId, messages)
    }
}
