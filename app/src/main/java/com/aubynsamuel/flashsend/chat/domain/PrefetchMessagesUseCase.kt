package com.aubynsamuel.flashsend.chat.domain

import com.aubynsamuel.flashsend.chat.data.repository.MessageRepository
import javax.inject.Inject

class PrefetchMessagesUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
) {

    suspend operator fun invoke(roomId: String) {
        messageRepository.prefetchNewMessagesForRoom(roomId)
    }
}