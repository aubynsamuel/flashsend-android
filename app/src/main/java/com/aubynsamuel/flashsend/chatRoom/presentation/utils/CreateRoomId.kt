package com.aubynsamuel.flashsend.chatRoom.presentation.utils

fun createRoomId(userId: String, currentUserId: String): String {
    val ids = listOf(userId, currentUserId)
    return ids.sorted().joinToString("_")
}