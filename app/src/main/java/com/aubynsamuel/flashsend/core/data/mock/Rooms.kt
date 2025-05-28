package com.aubynsamuel.flashsend.core.data.mock

import com.aubynsamuel.flashsend.core.model.RoomData
import com.google.firebase.Timestamp

@Suppress("unused")
val mockRooms = listOf(
    RoomData(
        roomId = "room_1",
        lastMessage = "Hey, how's it going?",
        lastMessageTimestamp = Timestamp.now(),
        lastMessageSenderId = "user_1",
        otherParticipant = mockUsers[1]
    ),
    RoomData(
        roomId = "room_2",
        lastMessage = "Are we still on for tonight?",
        lastMessageTimestamp = Timestamp.now(),
        lastMessageSenderId = "user_2",
        otherParticipant = mockUsers[0]
    ),
    RoomData(
        roomId = "room_3",
        lastMessage = "See you later!",
        lastMessageTimestamp = Timestamp.now(),
        lastMessageSenderId = "user_3",
        otherParticipant = mockUsers[2]
    ),
    RoomData(
        roomId = "room_4",
        lastMessage = "See you soon!",
        lastMessageTimestamp = Timestamp.now(),
        lastMessageSenderId = "user_3",
        otherParticipant = mockUsers[3]
    ),
)
