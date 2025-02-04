package com.aubynsamuel.flashsend.chatRoom

import androidx.compose.animation.AnimatedVisibility
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

@Composable
fun ChatMessage(
    message: Message,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var showPopup by remember { mutableStateOf(false) }
    Row(
        modifier = modifier,
        horizontalArrangement = if (message.isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (message.isFromMe) Alignment.End else Alignment.Start
        ) {
            // Message Bubble
            Surface(
                Modifier.clickable { showPopup = !showPopup },
                color = if (message.isFromMe) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = message.text,
                        color = if (message.isFromMe) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )

                    // Time and read status
                    Row(
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = message.time.toString(),
                            color = if (message.isFromMe) MaterialTheme.colorScheme.onPrimary.copy(
                                alpha = 0.7f
                            )
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            softWrap = true
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (message.isFromMe) "✓✓" else "",
                            fontSize = 10.sp
                        )
                    }
                    AnimatedVisibility(expanded) {
                        Text(text = "JetPack Compose")
                    }
                }
            }
            // Pass the state and a lambda to update it on dismissal
            PopUpMenu(
                expanded = showPopup,
                onDismiss = { showPopup = false },
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
