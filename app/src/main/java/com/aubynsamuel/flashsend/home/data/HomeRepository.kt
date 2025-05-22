package com.aubynsamuel.flashsend.home.data

import com.aubynsamuel.flashsend.core.domain.logger
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    val tag = "HomeRepository"

    fun getUnreadMessages(
        roomId: String,
        otherUserId: String,
        callBack: (value: Int) -> Unit,
    ): ListenerRegistration {
        val listener = firestore.collection("rooms").document(roomId).collection("messages")
            .where(Filter.equalTo("read", false)).where(Filter.equalTo("senderId", otherUserId))
            .addSnapshotListener { snapShot, error ->
                if (error != null) {
                    logger(tag, error.message.toString())
                    return@addSnapshotListener
                }
                snapShot?.let {
                    callBack(it.documents.size)
                }
            }
        return listener
    }
}