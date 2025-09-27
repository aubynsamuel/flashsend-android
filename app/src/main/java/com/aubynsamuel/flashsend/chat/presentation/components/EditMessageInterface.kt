package com.aubynsamuel.flashsend.chat.presentation.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.aubynsamuel.flashsend.chat.presentation.viewmodels.ChatViewModel
import com.aubynsamuel.flashsend.core.data.ConnectivityStatus
import com.aubynsamuel.flashsend.core.domain.model.ChatMessage
import com.aubynsamuel.flashsend.core.presentation.utils.showToast

@Composable
fun EditMessageDialog(
    roomId: String,
    message: ChatMessage,
    onDismiss: () -> Unit,
    onMessageEdited: (ChatMessage) -> Unit,
    chatViewModel: ChatViewModel,
    connectivityStatus: ConnectivityStatus,
) {
    var editText by remember { mutableStateOf(message.content) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Message") },
        text = {
            TextField(
                value = editText,
                onValueChange = { editText = it },
                label = { Text("Message") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (connectivityStatus is ConnectivityStatus.Available) {
                        if (editText.isNotBlank()) {
                            chatViewModel.updateMessage(
                                messageId = message.id,
                                newContent = editText,
                                onSuccess = {
                                    // Create a copy of the message with updated content.
                                    val updatedMessage = message.copy(content = editText)
                                    onMessageEdited(updatedMessage)
                                    showToast(context, "Message edited successfully")
                                },
                                onFailure = { e ->
                                    showToast(context, "Failed to update message")
                                },
                            )
                        }
                    } else {
                        showToast(context, "No internet connection")
                    }
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
