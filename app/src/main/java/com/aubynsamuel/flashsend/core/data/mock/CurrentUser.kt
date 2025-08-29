package com.aubynsamuel.flashsend.core.data.mock

import com.aubynsamuel.flashsend.core.domain.model.NewUser
import com.aubynsamuel.flashsend.core.domain.model.User

val CurrentUser = User(
    userId = "12345",
    username = "Samuel",
    profileUrl = "",
    deviceToken = "PSd0239323"
)

val NewLoggedInUser = NewUser(
    userId = "12345",
    username = "Eric",
    profileUrl = "",
    deviceToken = "23434003434",
    email = "eric_129203@gmail.com"
)