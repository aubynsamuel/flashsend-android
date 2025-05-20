package com.aubynsamuel.flashsend.navigation

import android.net.Uri
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

sealed class Screen(val route: String) {
    object ChatRoom : Screen("chatRoom/{username}/{userId}/{deviceToken}/{profileUrl}") {
        fun createRoute(
            username: String,
            userId: String,
            deviceToken: String,
            profileUrl: String,
        ) = "chatRoom/$username/$userId/$deviceToken/$profileUrl"
    }

    object ImagePreview :
        Screen("imagePreview/{imageUri}/{roomId}/{takenFromCamera}/{profileUrl}/{recipientsToken}") {
        fun createRoute(
            imageUri: String,
            roomId: String,
            takenFromCamera: Boolean,
            profileUrl: String,
            recipientsToken: String
        ): String {
            val encodedImageUri = Uri.encode(imageUri)
            val encodedRoomId = Uri.encode(roomId)
            val encodedProfileUrl = Uri.encode(profileUrl)
            val encodedRecipientsToken = Uri.encode(recipientsToken)
            val cameraFlag = if (takenFromCamera) "1" else "0"
            return "imagePreview/$encodedImageUri/$encodedRoomId/$cameraFlag/$encodedProfileUrl/$encodedRecipientsToken"
        }
    }

    object CameraX : Screen("cameraScreen/{roomId}/{profileUrl}/{deviceToken}") {
        fun createRoute(
            roomId: String, profileUrl: String, deviceToken: String
        ): String {
            val encodedRoomId = Uri.encode(roomId)
            val encodedProfileUrl = Uri.encode(profileUrl)
            val encodedDeviceToken = Uri.encode(deviceToken)
            return "cameraScreen/$encodedRoomId/$encodedProfileUrl/$encodedDeviceToken"
        }
    }
}