package com.aubynsamuel.flashsend.navigation

import kotlinx.serialization.Serializable

@Serializable
object AuthScreen

@Serializable
object LoadingScreen

@Serializable
data class MainScreen(val initialPage: Int = 0)

@Serializable
object EditProfileDC

@Serializable
object SetUserDetailsDC

@Serializable
object SearchUsersScreenDC

@Serializable
data class OtherProfileScreenDC(
    val user: String,
)

@Serializable
data class ChatRoomScreen(
    val username: String = "",
    val userId: String,
    val deviceToken: String = "",
    val profileUrl: String = "",
)

@Serializable
data class ImagePreviewScreen(
    val imageUri: String,
    val roomId: String,
    val takenFromCamera: Boolean,
    val profileUrl: String = "",
    val recipientsToken: String = "",
)

@Serializable
data class CameraXScreenDC(val roomId: String, val profileUrl: String, val deviceToken: String)
