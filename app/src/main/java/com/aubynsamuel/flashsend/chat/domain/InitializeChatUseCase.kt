package com.aubynsamuel.flashsend.chat.domain

import com.aubynsamuel.flashsend.chat.data.repository.MessageRepository
import javax.inject.Inject

class InitializeChatUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
) {
    suspend operator fun invoke(roomId: String, currentUserId: String, otherUserId: String) {
        messageRepository.createRoomIfNeeded(roomId, currentUserId, otherUserId)
    }
}