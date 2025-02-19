package com.aubynsamuel.flashsend.notifications

class NotificationRepository {
    private val api = RetrofitClient.apiService

    // Send a push notification
    suspend fun sendNotification(
        recipientsToken: String,
        title: String,
        body: String,
        roomId: String,
        recipientsUserId: String,
        sendersUserId: String,
        profileUrl: String
    ): ApiResponse {
        val request = SendNotificationRequest(
            recipientsToken, title, body, roomId, recipientsUserId, sendersUserId, profileUrl
        )
        return api.sendNotification(request)
    }

    // Send a reply (also updates Firestore on the server and sends a notification)
    suspend fun sendReply(
        sendersUserId: String,
        recipientsUserId: String,
        roomId: String,
        replyText: String
    ): ApiResponse {
        val request = ReplyRequest(sendersUserId, recipientsUserId, roomId, replyText)
        return api.sendReply(request)
    }

    // Mark messages as read
    suspend fun markMessagesAsRead(
        sendersUserId: String,
        roomId: String
    ): ApiResponse {
        val request = MarkAsReadRequest(sendersUserId, roomId)
        return api.markMessagesAsRead(request)
    }

    suspend fun checkServerHealth(): HealthResponse {
        return api.getHealthStatus()
    }
}