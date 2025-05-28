package com.aubynsamuel.flashsend.core.data.mock

import com.aubynsamuel.flashsend.core.model.NewUser
import com.aubynsamuel.flashsend.core.model.User

val CurrentUser = User(
    userId = "12345",
    username = "Samuel",
    profileUrl = "",
    deviceToken = "12233434"
)

val NewLoggedInUser = NewUser(
    userId = "12345",
    username = "Samuel",
    profileUrl = "",
    deviceToken = "12233434",
    email = "aubynsamuel05@gmail.com"
)