package com.aubynsamuel.flashsend.notifications.data.model

/**
 * Request for sending a notification
 */
data class SendNotificationRequest(
    val recipientsToken: String,
    val title: String,
    val body: String,
    val roomId: String,
    val recipientsUserId: String,
    val sendersUserId: String,
    val profileUrl: String,
)

/**
 * Request for sending a reply
 */
data class ReplyRequest(
    val sendersUserId: String,
    val recipientsUserId: String,
    val roomId: String,
    val replyText: String,
)

/**
 * Request for marking messages as read
 */
data class MarkAsReadRequest(
    val sendersUserId: String,
    val roomId: String,
)

/**
 * A generic response from server endpoints
 */
data class ApiResponse(
    val success: Boolean,
    val message: String?,
    val error: String?,
)

data class HealthResponse(
    val status: String,
)
