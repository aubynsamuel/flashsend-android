package com.aubynsamuel.flashsend.chatRoom

import android.widget.Toast
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.aubynsamuel.flashsend.functions.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun EditMessageDialog(
    roomId: String,
    message: ChatMessage,
    initialText: String,
    onDismiss: () -> Unit,
    onMessageEdited: (ChatMessage) -> Unit,
    coroutineScope: CoroutineScope,

    ) {
    var editText by remember { mutableStateOf(initialText) }
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
                    if (editText.isNotBlank()) {
                        coroutineScope.launch {
                            updateMessageInFirebase(
                                roomId = roomId,
                                messageId = message.id,
                                newContent = editText,
                                onSuccess = {
                                    // Create a copy of the message with updated content.
                                    val updatedMessage = message.copy(content = editText)
                                    onMessageEdited(updatedMessage)
                                    Toast.makeText(
                                        context,
                                        "Message edited successfully",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                },
                                onFailure = { e ->
                                    Toast.makeText(
                                        context,
                                        "Failed to update message",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                context = context
                            )
                        }
                    }
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
