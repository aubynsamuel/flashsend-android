package com.aubynsamuel.flashsend.chatRoom.domain

import android.content.Context
import com.aubynsamuel.flashsend.chatRoom.data.repository.MessageRepository
import javax.inject.Inject

class UpdateMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
    private val context: Context,
) {
    operator fun invoke(
        roomId: String,
        messageId: String,
        newContent: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        messageRepository.updateMessage(
            roomId,
            messageId,
            newContent,
            onSuccess,
            onFailure,
            context
        )
    }
}