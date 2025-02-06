package com.aubynsamuel.flashsend.chatRoom

import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatMessage(
    message: ChatMessage,
    isFromMe: Boolean,
    modifier: Modifier = Modifier,
) {
    var showPopup by remember { mutableStateOf(false) }
    Row(
        modifier = modifier,
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            Modifier.clickable { showPopup = !showPopup },
            color = if (isFromMe) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.absoluteOffset(x = 60.dp, y = 30.dp)) {
                PopUpMenu(
                    expanded = showPopup, onDismiss = { showPopup = false }, modifier = Modifier
                )
            }
            Column(
                modifier = Modifier.padding(start = 8.dp, end = 30.dp, top = 2.dp, bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy((-5).dp)
            ) {
                Text(
                    text = message.content,
                    color = if (isFromMe) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .absoluteOffset(x = 22.dp)
                ) {
                    Text(
                        text = formatMessageTime(message.createdAt),
                        color = if (isFromMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
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

val message: ChatMessage = ChatMessage(
    id = "12",
    content = "Whats up with lalks klaksa aksmas asklasdjads asaisdpoasd asdias",
    createdAt = Date(),
    senderId = "",
    senderName = "",
    read = true,
    delivered = true
)

@Preview
@Composable
fun PrevChatMessage() {
    ChatMessage(message, isFromMe = true)
}
