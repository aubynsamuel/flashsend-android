package com.aubynsamuel.flashsend.chatRoom.domain

import com.aubynsamuel.flashsend.chatRoom.data.repository.MessageRepository
import com.aubynsamuel.flashsend.core.model.ChatMessage
import javax.inject.Inject

class GetMessagesUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
) {
    suspend operator fun invoke(roomId: String): List<ChatMessage> {
        return messageRepository.getMessagesForRoom(roomId)
    }
}