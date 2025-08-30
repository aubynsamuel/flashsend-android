package com.aubynsamuel.flashsend.core.domain.model

import com.google.firebase.Timestamp
import java.io.Serializable

data class RoomData(
    var roomId: String = "",
    var lastMessage: String = "",
    var lastMessageTimestamp: Timestamp?,
    var lastMessageSenderId: String = "",
    var otherParticipant: User
) : Serializable