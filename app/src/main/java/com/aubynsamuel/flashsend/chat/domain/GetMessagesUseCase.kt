package com.aubynsamuel.flashsend.chat.domain

import com.aubynsamuel.flashsend.chat.data.repository.MessageRepository
import com.aubynsamuel.flashsend.core.domain.model.ChatMessage
import javax.inject.Inject

class GetMessagesUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
) {
    suspend operator fun invoke(roomId: String): List<ChatMessage> {
        return messageRepository.getMessagesForRoom(roomId)
    }
}