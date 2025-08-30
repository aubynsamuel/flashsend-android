package com.aubynsamuel.flashsend.chat.domain

import com.aubynsamuel.flashsend.chat.data.repository.MessageRepository
import javax.inject.Inject

class RemoveMessageListenerUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
) {
    operator fun invoke(listener: Any) {
        messageRepository.removeMessageListener(listener)
    }
}