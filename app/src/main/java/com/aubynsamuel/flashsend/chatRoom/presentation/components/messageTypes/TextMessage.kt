package com.aubynsamuel.flashsend.chatRoom.presentation.components.messageTypes

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import com.aubynsamuel.flashsend.core.model.ChatMessage

@Composable
fun TextMessage(message: ChatMessage, isFromMe: Boolean, fontSize: Int = 16) {
    Text(
        text = message.content,
        color = if (isFromMe) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        fontSize = fontSize.sp,
        lineHeight = getLineHeight(fontSize).sp
    )
}

fun getLineHeight(fontSize: Int): Int {
    var lineHeight = 0
    when (fontSize) {
        12 -> lineHeight = 15
        14 -> lineHeight = 18
        16 -> lineHeight = 21
        18 -> lineHeight = 24
        20 -> lineHeight = 27
    }
    return lineHeight
}