package com.aubynsamuel.flashsend.chat.presentation.components

import android.content.Context
import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.aubynsamuel.flashsend.chat.data.local.ChatDatabase
import com.aubynsamuel.flashsend.core.data.ConnectivityStatus
import com.aubynsamuel.flashsend.core.domain.model.ChatMessage
import com.aubynsamuel.flashsend.core.presentation.utils.showToast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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
    onDeletionFailure: () -> Unit,
    connectivityStatus: ConnectivityStatus,
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
                        if (connectivityStatus is ConnectivityStatus.Available) {
                            handleDelete(
                                message, roomId, onMessageDeleted,
                                context, onDeletionFailure
                            )
                        } else {
                            showToast(context, "No internet connection")
                        }
                        onDismiss()
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
    onDeletionFailure: () -> Unit,
) {
    try {
        val db = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReferenceFromUrl(
            if (message.type == "image") message.image.toString()
            else message.audio.toString()
        )
        val roomRef = db.collection("rooms").document(roomId)
        val messageRef = roomRef.collection("messages").document(message.id)
        val messageDao = ChatDatabase.Companion.getDatabase(context).messageDao()

        messageRef.delete()
            .addOnSuccessListener {
                onMessageDeleted()
                GlobalScope.launch { messageDao.deleteMessage(message.id) }
                if (message.type == "image" || message.type == "audio") {
                    storageRef.delete()
                }
            }
            .addOnFailureListener { error ->
                onDeletionFailure()
            }
    } catch (error: Exception) {
        Log.d("DeleteMessageTag", "Failed to delete message: ${error.message}")
    }
}