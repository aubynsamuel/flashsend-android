package com.aubynsamuel.flashsend.chatRoom.messageTypes

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import com.aubynsamuel.flashsend.functions.ChatMessage

@Composable
fun TextMessage(message: ChatMessage, isFromMe: Boolean, fontSize: Int = 16) {
    Text(
        text = message.content,
        color = if (isFromMe) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        fontSize = fontSize.sp
    )
}