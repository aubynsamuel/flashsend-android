package com.aubynsamuel.flashsend.chatRoom.domain

import com.aubynsamuel.flashsend.chatRoom.data.remote.MessageRepository
import com.aubynsamuel.flashsend.core.model.ChatMessage
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