package com.aubynsamuel.flashsend.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aubynsamuel.flashsend.chatRoom.messageTypes.AudioMessage
import com.aubynsamuel.flashsend.chatRoom.messageTypes.ImageMessage
import com.aubynsamuel.flashsend.chatRoom.messageTypes.LocationMessage
import com.aubynsamuel.flashsend.chatRoom.messageTypes.TextMessage
import com.aubynsamuel.flashsend.functions.ChatMessage
import com.aubynsamuel.flashsend.mockData.messageExample
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DemoMessage(
    message: ChatMessage,
    isFromMe: Boolean,
    modifier: Modifier = Modifier,
    fontSize: Int
) {
    Row(
        modifier = modifier,
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
    ) {
//       Content
        Surface(
            color = if (isFromMe) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.absoluteOffset(x = 60.dp, y = 30.dp)) {
            }
//            different rendering for different message types
            Column(
                modifier = Modifier.padding(
                    start = if (message.type == "text") 8.dp else 0.dp,
                    end = if (message.type == "text") 30.dp else 0.dp,
                    top = if (message.type == "text") 2.dp else 0.dp,
                    bottom = 0.dp
                ),
                verticalArrangement = Arrangement.spacedBy((-5).dp)
            ) {
                when (message.type) {
                    "text" -> {
                        TextMessage(message = message, isFromMe = isFromMe, fontSize = fontSize)
                    }

                    "image" -> {
                        ImageMessage(
                            message = message,
                            isFromMe = isFromMe,
                            fontSize = fontSize,
                            showPopUp = {},
                        )
                    }

                    "audio" -> {
                        AudioMessage(
                            message = message,
                            isFromMe = isFromMe,
                            fontSize = fontSize
                        )
                    }

                    "location" -> {
                        LocationMessage(
                            message = message,
                            showPopUp = {},
                        )
                    }

                    else -> {
                        // Fallback to a text message
                        TextMessage(message = message, isFromMe = isFromMe)
                    }
                }
//                Message time and read status
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .absoluteOffset(x = if (message.type == "text") 22.dp else 0.dp)
                ) {
                    Text(
                        text = formatMessageTime(message.createdAt),
                        color = if (isFromMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isFromMe) {
                            if (message.read) "✓✓" else "✓"
                        } else "", fontSize = 12.sp

                    )
                }
            }
        }

    }
}

fun formatMessageTime(date: Date): String {
    val formater = SimpleDateFormat("h:m a", Locale.US)
    return formater.format(date).lowercase()
}

@Preview(apiLevel = 35)
@Composable
fun PrevChatMessage() {
    DemoMessage(
        messageExample, isFromMe = true,
        modifier = Modifier,
        fontSize = 20
    )
}
