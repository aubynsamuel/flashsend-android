package com.aubynsamuel.flashsend.chatRoom

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
fun updateMessageInFirebase(
    roomId: String,
    messageId: String,
    newContent: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit,
    context: Context
) {
    val messageDao = ChatDatabase.getDatabase(context).messageDao()
    val db = FirebaseFirestore.getInstance()

    val messageRef = db.collection("rooms")
        .document(roomId)
        .collection("messages")
        .document(messageId)

    messageRef.update("content", newContent)
        .addOnSuccessListener {
            onSuccess()
            GlobalScope.launch { messageDao.editMessage(messageId, newContent) }
        }
        .addOnFailureListener { exception -> onFailure(exception) }
}
