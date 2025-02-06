package com.aubynsamuel.flashsend.mockData

import com.aubynsamuel.flashsend.home.RoomData
import com.aubynsamuel.flashsend.home.User

val mockUsers = listOf(
    User(
        userId = "user_1",
        username = "Alice Johnson",
        profileUrl = "https://encrypted-tbn0.gstatic.com/i" +
                "mages?q=tbn:ANd9GcSQJ2sZUUc6xap9g__-HYUVi9LA" +
                "2MnfG8_7xF33YejfCkudFpb2voAVK" +
                "P3K2kg9RBHVo4gFx5saiDaNNzxhhMLjPg",
        otherUsersDeviceToken = "token_1"
    ),
    User(
        userId = "user_2",
        username = "Bob Smith",
        profileUrl = "",
        otherUsersDeviceToken = "token_2"
    ),
    User(
        userId = "user_3",
        username = "Charlie Brown",
        profileUrl = "",
        otherUsersDeviceToken = "token_3"
    ),
    User(
        userId = "user_4",
        username = "Chris Brown",
        profileUrl = "",
        otherUsersDeviceToken = "token_4"
    )
)

val mockRooms = listOf(
    RoomData(
        roomId = "room_1",
        lastMessage = "Hey, how's it going?",
        lastMessageTimestamp = System.currentTimeMillis() - 60000,
        lastMessageSenderId = "user_1",
        otherParticipant = mockUsers[1] // Bob Smith
    ),
    RoomData(
        roomId = "room_2",
        lastMessage = "Are we still on for tonight?",
        lastMessageTimestamp = System.currentTimeMillis() - 120000,
        lastMessageSenderId = "user_2",
        otherParticipant = mockUsers[0] // Alice Johnson
    ),
    RoomData(
        roomId = "room_3",
        lastMessage = "See you later!",
        lastMessageTimestamp = System.currentTimeMillis() - 180000,
        lastMessageSenderId = "user_3",
        otherParticipant = mockUsers[2] // Charlie Brown
    ),
    RoomData(
        roomId = "room_4",
        lastMessage = "See you soon!",
        lastMessageTimestamp = System.currentTimeMillis() - 182000,
        lastMessageSenderId = "user_3",
        otherParticipant = mockUsers[3] // Charlie Brown
    )
)
