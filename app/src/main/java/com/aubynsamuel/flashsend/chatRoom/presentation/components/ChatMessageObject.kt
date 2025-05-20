package com.aubynsamuel.flashsend.chatRoom.presentation.components

import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aubynsamuel.flashsend.chatRoom.domain.ChatViewModel
import com.aubynsamuel.flashsend.chatRoom.domain.vibrateDevice
import com.aubynsamuel.flashsend.chatRoom.presentation.components.messageTypes.AudioMessage
import com.aubynsamuel.flashsend.chatRoom.presentation.components.messageTypes.ImageMessage
import com.aubynsamuel.flashsend.chatRoom.presentation.components.messageTypes.LocationMessage
import com.aubynsamuel.flashsend.chatRoom.presentation.components.messageTypes.TextMessage
import com.aubynsamuel.flashsend.core.data.mock.messageExample
import com.aubynsamuel.flashsend.core.domain.copyTextToClipboard
import com.aubynsamuel.flashsend.core.domain.formatMessageTime
import com.aubynsamuel.flashsend.core.model.ChatMessage

@Composable
fun ChatMessageObject(
    message: ChatMessage,
    isFromMe: Boolean,
    modifier: Modifier = Modifier,
    roomId: String = "",
    fontSize: Int,
    chatViewModel: ChatViewModel,
    currentUserId: String
) {
    var showPopup by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Row(
        modifier = modifier.padding(
            end = if (!isFromMe && message.type == "image") 30.dp else 0.dp,
            start = if (isFromMe && message.type == "image") 30.dp else 0.dp
        ), horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
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
                    context, "Message could not be deleted, Try again", Toast.LENGTH_SHORT
                ).show()
            },
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
            )
        }
//       Content
        Surface(
            Modifier.pointerInput(Unit) {
                detectTapGestures(onLongPress = {
                    vibrateDevice(context)
                    showPopup = !showPopup
                })
            }, color = if (isFromMe) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.absoluteOffset(x = 30.dp, y = 30.dp)) {
                val myMessageOptionsList = listOf<DropMenu>(
                    DropMenu(
                        text = "Copy",
                        onClick = { copyTextToClipboard(context, message.content) },
                        icon = Icons.Default.CopyAll
                    ),
                    DropMenu(
                        text = "Edit", onClick = {
                            showEditDialog = true
                        }, icon = Icons.Default.Edit
                    ),
                    DropMenu(
                        text = "Delete",
                        onClick = { showDeleteDialog = true },
                        icon = Icons.Default.Delete
                    ),
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
                    dropItems = (if (isFromMe) myMessageOptionsList else othersMessageOptionsList),
                    reactions = {
                        ReactionPicker(onReactionSelected = { selectedEmoji ->
                            chatViewModel.addReactionToMessage(
                                roomId = roomId,
                                messageId = message.id,
                                userId = if (isFromMe) currentUserId else message.senderId,
                                emoji = selectedEmoji,
                                messageContent = if (message.type == "image") {
                                    "an image 📷"
                                } else if (message.type == "audio") {
                                    "an audio  ${message.content}"
                                } else if (message.type == "location") {
                                    "a location"
                                } else "\"${message.content}\""
                            )
                            showPopup = false
                        })
                    })
            }
//            different rendering for different message types
            Column(
                modifier = Modifier.padding(
                    start = if (message.type == "text") 8.dp else 0.dp,
                    end = if (message.type == "text") 30.dp else 0.dp,
                    top = if (message.type == "text") 2.dp else 0.dp,
                    bottom = 0.dp
                ), verticalArrangement = Arrangement.spacedBy((-5).dp)
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
                            showPopUp = {
                                showPopup = !showPopup
                            }
                        )
                    }

                    "audio" -> {
                        AudioMessage(
                            message = message, isFromMe = isFromMe, fontSize = fontSize
                        )
                    }

                    "location" -> {
                        LocationMessage(
                            message = message,
                            showPopUp = {
                                showPopup = !showPopup
                            }
                        )
                    }

                    else -> {
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
                    Spacer(modifier = Modifier.width(4.dp))
                    if (message.reactions.isNotEmpty()) {
                        MessageReaction(message)
                    }
                }

            }
        }
    }
}

@Composable
fun MessageReaction(message: ChatMessage) {
    Row {
        message.reactions.values.distinct().forEach { emoji ->
            Text(
                text = emoji, fontSize = 12.sp, modifier = Modifier.padding(end = 4.dp)
            )
        }
    }
}

@Preview
@Composable
fun PrevChatMessage() {
    ChatMessageObject(
        messageExample,
        isFromMe = true,
        modifier = Modifier,
        roomId = "",
        fontSize = 15,
        chatViewModel = ChatViewModel(LocalContext.current),
        currentUserId = ""
    )
}
