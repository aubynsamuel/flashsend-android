package com.aubynsamuel.flashsend.chat.domain

import com.aubynsamuel.flashsend.chat.data.repository.MessageRepository
import com.aubynsamuel.flashsend.core.domain.model.ChatMessage
import javax.inject.Inject

class AddMessageListenerUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
) {
    operator fun invoke(
        roomId: String,
        onMessagesUpdated: (List<ChatMessage>) -> Unit,
        onError: (String) -> Unit,
    ): Any {
        return messageRepository.addMessageListener(roomId, onMessagesUpdated, onError)
    }
}