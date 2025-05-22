package com.aubynsamuel.flashsend.chatRoom.domain

import com.aubynsamuel.flashsend.chatRoom.data.remote.MessageRepository
import javax.inject.Inject

class InitializeChatUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
) {
    suspend operator fun invoke(roomId: String, currentUserId: String, otherUserId: String) {
        messageRepository.createRoomIfNeeded(roomId, currentUserId, otherUserId)
    }
}