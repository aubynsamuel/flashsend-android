package com.aubynsamuel.flashsend.core.model

import androidx.annotation.Keep
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
) : Serializable

@Keep
data class ChatMessage(
    var id: String = "",
    var content: String = "",
    var image: String? = null,
    var audio: String? = null,
    var createdAt: Date = Date(),
    var senderId: String = "",
    var senderName: String = "",
    var replyTo: String? = null,
    var read: Boolean = false,
    var type: String = "text",
    var delivered: Boolean = false,
    var location: Location? = null,
    var duration: Long? = null,
    var reactions: MutableMap<String, String> = mutableMapOf()
)

data class Location(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
)

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