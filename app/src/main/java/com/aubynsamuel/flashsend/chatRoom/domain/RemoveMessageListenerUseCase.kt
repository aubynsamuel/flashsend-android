package com.aubynsamuel.flashsend.chatRoom.domain

import com.aubynsamuel.flashsend.chatRoom.data.repository.MessageRepository
import javax.inject.Inject

class RemoveMessageListenerUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
) {
    operator fun invoke(listener: Any) {
        messageRepository.removeMessageListener(listener)
    }
}