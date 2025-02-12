package com.aubynsamuel.flashsend.chatRoom

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore

suspend fun updateMessageInFirebase(
    roomId: String,
    messageId: String,
    newContent: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit,
    context: Context
) {
    val messageDao = ChatDatabase.getDatabase(context).messageDao()
    messageDao.editMessage(messageId, newContent)
    val db = FirebaseFirestore.getInstance()

    val messageRef = db.collection("rooms")
        .document(roomId)
        .collection("messages")
        .document(messageId)

    messageRef.update("content", newContent)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
}
