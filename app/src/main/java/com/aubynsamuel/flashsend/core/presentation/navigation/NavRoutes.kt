package com.aubynsamuel.flashsend.core.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Represents the authentication screen of the app.
 * This is where users log in or sign up.
 */
@Serializable
object AuthScreenDC

/**
 * Represents the first screen shown when the app starts.
 */
@Serializable
object LoadingScreenDC

/**
 * Represents the main screen of the app.
 * @param initialPage The initial page to display within the
 * main screen, defaults to 0.
 */
@Serializable
data class MainScreen(val initialPage: Int = 0)

/**
 * Represents the screen for editing the current user's profile.
 */
@Serializable
object EditProfileDC

/**
 * Represents the screen for setting up user details after sign up.
 */
@Serializable
object SetUserDetailsDC

/**
 * Represents the screen for searching other users.
 */
@Serializable
object SearchUsersScreenDC

/**
 * Represents the profile screen of another user.
 * @param user A json string containing the user data for the user
 * whose profile is to be displayed.
 */
@Serializable
data class OtherProfileScreenDC(
    val user: String,
)

/**
 * Represents the chat room screen for a specific conversation.
 * @param username The display name of the other user in the chat.
 * @param userId The unique identifier of the other user in the chat.
 * @param deviceToken The device token of the other user, for notifications.
 * @param profileUrl The URL of the other user's profile picture.
 */
@Serializable
data class ChatRoomScreen(
    val username: String = "",
    val userId: String,
    val deviceToken: String = "",
    val profileUrl: String = "",
)

/**
 * Represents the screen for previewing an image before sending.
 * @param imageUri The URI of the image to be previewed.
 * @param takenFromCamera Indicates whether the image was captured directly from the camera.
 * @param recipientsToken The device token of the recipient, for notifications.
 */
@Serializable
data class ImagePreviewScreen(
    val imageUri: String,
    val takenFromCamera: Boolean,
    val recipientsToken: String = "",
)

/**
 * Represents the camera screen integrated with CameraX.
 * @param roomId The identifier of the chat room the captured media will be associated with.
 * @param deviceToken The device token of the recipient for notifications.
 */
@Serializable
data class CameraXScreenDC(val roomId: String, val deviceToken: String)

@Serializable
data class FullScreenImageViewerDC(val imageUri: String)