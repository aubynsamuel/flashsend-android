package com.aubynsamuel.flashsend.chatRoom.presentation.components

import android.content.Context
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.aubynsamuel.flashsend.chatRoom.data.ChatDatabase
import com.aubynsamuel.flashsend.core.model.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Composable
fun DeleteMessageDialog(
    message: ChatMessage,
    roomId: String,
    onDismiss: () -> Unit,
    onMessageDeleted: () -> Unit,
    showDialog: Boolean,
    onDeletionFailure: () -> Unit
) {
    val context = LocalContext.current

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text("Delete message") },
            text = { Text("This action cannot be undone, do you want to continue?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        handleDelete(
                            message, roomId, onMessageDeleted,
                            context, onDeletionFailure
                        )
                    }
                ) {
                    Text("Yes", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismiss()
                    }
                ) {
                    Text("No")
                }
            }
        )
    }
}

@OptIn(DelicateCoroutinesApi::class)
private fun handleDelete(
    message: ChatMessage,
    roomId: String,
    onMessageDeleted: () -> Unit,
    context: Context,
    onDeletionFailure: () -> Unit
) {
    try {
        val db = FirebaseFirestore.getInstance()
        val roomRef = db.collection("rooms").document(roomId)
        val messageRef = roomRef.collection("messages").document(message.id)
        val messageDao = ChatDatabase.Companion.getDatabase(context).messageDao()

        messageRef.delete()
            .addOnSuccessListener {
                onMessageDeleted()
                GlobalScope.launch { messageDao.deleteMessage(message.id) }
            }
            .addOnFailureListener { error ->
                onDeletionFailure
            }
    } catch (error: Exception) {
        println("Failed to delete message: ${error.message}")
    }
}