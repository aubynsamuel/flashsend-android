package com.aubynsamuel.flashsend.notifications

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            // Extract fields from the data payload
            val title = remoteMessage.data["title"] ?: "New Message"
            val body = remoteMessage.data["body"] ?: ""
            val roomId = remoteMessage.data["roomId"] ?: ""
            // Optionally extract other fields if needed:
            val recipientsUserId = remoteMessage.data["sendersUserId"] ?: ""
            val sendersUserId = remoteMessage.data["recipientsUserId"] ?: ""
            val profileUrl = remoteMessage.data["profileUrl"] ?: ""

            // Log for debugging
            Log.d("MyFirebaseMessagingSvc", "Data received: title=$title, roomId=$roomId")

            // Call custom notification function.
            // Note: Here, we pass roomId as the 'id' which is used to group messages (conversation ID).
            showNotification(
                context = this,
                title = title,
                message = body,
                sender = title,
                id = roomId,
                sendersUserId = sendersUserId,
                recipientsUserId = recipientsUserId,
            )
        }
    }

    val tag = "NotificationTokenChange"

    // Optionally handle token refresh
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(tag, "New FCM token: $token")
    }
}
