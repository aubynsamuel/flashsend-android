package com.aubynsamuel.flashsend.core.data.mock

import com.aubynsamuel.flashsend.core.model.User

val mockUsers = listOf(
    User(
        userId = "user_1",
        username = "Alice Johnson",
        profileUrl = "https://encrypted-tbn0.gstatic.com/i",
        deviceToken = "token_1"
    ),
    User(
        userId = "user_2",
        username = "Bob Smith",
        profileUrl = "",
        deviceToken = "token_2"
    ),
    User(
        userId = "user_3",
        username = "Charlie Brown",
        profileUrl = "",
        deviceToken = "token_3"
    ),
    User(
        userId = "user_4",
        username = "Chris Brown",
        profileUrl = "",
        deviceToken = "token_4"
    )
)