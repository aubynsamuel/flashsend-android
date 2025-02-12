package com.aubynsamuel.flashsend.functions

import com.google.firebase.Timestamp
import java.io.Serializable
import java.util.Date


data class RoomData(
    var roomId: String = "",
    var lastMessage: String = "",
    var lastMessageTimestamp: Timestamp?,
    var lastMessageSenderId: String = "",
    var otherParticipant: User
) : Serializable

data class User(
    var userId: String = "",
    var username: String = "",
    var profileUrl: String = "",
    var deviceToken: String = ""
) : Serializable

data class NewUser(
    var userId: String = "",
    var username: String = "",
    var profileUrl: String = "",
    var deviceToken: String = "",
    var email: String = ""
) {
    // Add a no-argument constructor
    constructor() : this("", "", "", "", "")
}

data class ChatMessage(
    val id: String = "",
    val content: String = "",
    val image: String? = null,
    val audio: String? = null,
    val createdAt: Date = Date(),
    val senderId: String = "",
    val senderName: String = "",
    val replyTo: String? = null,
    val read: Boolean = false,
    val type: String = "text",
    val delivered: Boolean = false,
    val location: Location? = null,
    val duration: Long? = null
)

data class Location(
    val latitude: Double,
    val longitude: Double
)

//settings
enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class SettingsState(
    val userName: String = "",
    val userStatus: String = "Online",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val fontSize: Int = 16,
    val notificationsEnabled: Boolean = true,
    val lastSeenVisible: Boolean = true,
    val readReceiptsEnabled: Boolean = true,
    val appVersion: String = "1.0.0"
)