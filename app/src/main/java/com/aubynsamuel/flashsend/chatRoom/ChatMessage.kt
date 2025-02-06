package com.aubynsamuel.flashsend.chatRoom

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Date

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
        Column(
            horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
        ) {
            Surface(
                Modifier.clickable { showPopup = !showPopup },
                color = if (isFromMe) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = message.content,
                        color = if (isFromMe) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )

                    Row(
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = formatMessageTime(message.createdAt),
                            color = if (isFromMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            softWrap = true
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isFromMe) {
                                if (message.read) "✓✓" else "✓"
                            } else "",
                            fontSize = 10.sp
                        )
                    }
                }
            }

            PopUpMenu(
                expanded = showPopup,
                onDismiss = { showPopup = false },
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

fun formatMessageTime(date: Date): String {
    // Implement your date formatting logic here
    return date.toString()
}