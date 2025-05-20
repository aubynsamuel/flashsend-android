package com.aubynsamuel.flashsend.notifications.services

import android.annotation.SuppressLint
import android.util.Log
import com.aubynsamuel.flashsend.notifications.domain.showNotification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val tag = "MyFirebaseMessagingService"
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "New Message"
            val body = remoteMessage.data["body"] ?: ""
            val roomId = remoteMessage.data["roomId"] ?: ""
            val recipientsUserId = remoteMessage.data["sendersUserId"] ?: ""
            val sendersUserId = remoteMessage.data["recipientsUserId"] ?: ""
            val profileUrl = remoteMessage.data["profileUrl"] ?: ""

            Log.d(tag, "Data received: title=$title, roomId=$roomId")

            showNotification(
                context = this,
                message = body,
                sender = title,
                id = roomId,
                sendersUserId = sendersUserId,
                recipientsUserId = recipientsUserId,
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(tag, "New FCM token: $token")
    }
}