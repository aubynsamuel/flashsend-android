package com.aubynsamuel.flashsend.core.domain.model

import java.io.Serializable

data class User(
    var userId: String = "",
    var username: String = "",
    var profileUrl: String = "",
    var deviceToken: String = "",
) : Serializable

data class NewUser(
    var userId: String = "",
    var username: String = "",
    var profileUrl: String = "",
    var deviceToken: String = "",
    var email: String = "",
) : Serializable