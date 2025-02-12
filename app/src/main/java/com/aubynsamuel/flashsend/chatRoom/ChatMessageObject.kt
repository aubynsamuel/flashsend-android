package com.aubynsamuel.flashsend.chatRoom

import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aubynsamuel.flashsend.chatRoom.messageTypes.AudioMessage
import com.aubynsamuel.flashsend.chatRoom.messageTypes.ImageMessage
import com.aubynsamuel.flashsend.chatRoom.messageTypes.LocationMessage
import com.aubynsamuel.flashsend.chatRoom.messageTypes.TextMessage
import com.aubynsamuel.flashsend.functions.ChatMessage
import com.aubynsamuel.flashsend.functions.copyTextToClipboard
import kotlinx.coroutines.CoroutineScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatMessageObject(
    message: ChatMessage,
    isFromMe: Boolean,
    modifier: Modifier = Modifier,
    roomId: String = "",
    coroutineScope: CoroutineScope, fontSize: Int
) {
    var showPopup by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Row(
        modifier = modifier,
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
    ) {
//        Action pop ups
        DeleteMessageDialog(
            message = message,
            roomId = roomId,
            onDismiss = {
                showDeleteDialog = false
            },
            onMessageDeleted = {
                Toast.makeText(context, "Message has been deleted", Toast.LENGTH_SHORT).show()
            },
            onDeletionFailure = {
                Toast.makeText(
                    context,
                    "Message could not be deleted, Try again",
                    Toast.LENGTH_SHORT
                ).show()
            },
            coroutineScope = coroutineScope,
            showDialog = showDeleteDialog,
        )
        if (showEditDialog) {
            EditMessageDialog(
                roomId = roomId,
                message = message,
                initialText = message.content,
                onDismiss = { showEditDialog = false },
                onMessageEdited = { updatedMessage ->
                    showEditDialog = false
                },
                coroutineScope = coroutineScope,

                )
        }
//       Content
        Surface(
            Modifier.clickable { showPopup = !showPopup },
            color = if (isFromMe) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.absoluteOffset(x = 60.dp, y = 30.dp)) {
                val myMessageOptionsList = listOf<DropMenu>(
                    DropMenu(
                        text = "Copy",
                        onClick = { copyTextToClipboard(context, message.content) },
                        icon = Icons.Default.CopyAll
                    ), DropMenu(
                        text = "Delete",
                        onClick = { showDeleteDialog = true },
                        icon = Icons.Default.Delete
                    ), DropMenu(
                        text = "Edit",
                        onClick = {
                            showEditDialog = true
                        },
                        icon = Icons.Default.Edit
                    )
                )
                val othersMessageOptionsList = listOf<DropMenu>(
                    DropMenu(
                        text = "Copy",
                        onClick = { copyTextToClipboard(context, message.content) },
                        icon = Icons.Default.CopyAll
                    )
                )
                PopUpMenu(
                    expanded = showPopup,
                    onDismiss = { showPopup = false },
                    modifier = Modifier,
                    dropItems = (if (isFromMe) myMessageOptionsList else othersMessageOptionsList)
                )
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
                        )
                    }

                    "audio" -> {
                        AudioMessage(
                            message = message,
                            isFromMe = isFromMe
                        )
                    }

                    "location" -> {
                        LocationMessage(message = message)
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

//@Preview
//@Composable
//fun PrevChatMessage() {
//    ChatMessage(message, isFromMe = true)
//}
