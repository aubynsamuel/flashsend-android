package com.aubynsamuel.flashsend

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aubynsamuel.flashsend.auth.AuthRepository
import com.aubynsamuel.flashsend.auth.AuthScreen
import com.aubynsamuel.flashsend.auth.AuthViewModel
import com.aubynsamuel.flashsend.auth.SetUserDetailsScreen
import com.aubynsamuel.flashsend.chatRoom.CameraXScreen
import com.aubynsamuel.flashsend.chatRoom.ChatScreen
import com.aubynsamuel.flashsend.chatRoom.ChatViewModel
import com.aubynsamuel.flashsend.chatRoom.ImagePreviewScreen
import com.aubynsamuel.flashsend.chatRoom.OtherUserProfile
import com.aubynsamuel.flashsend.functions.User
import com.aubynsamuel.flashsend.functions.logger
import com.aubynsamuel.flashsend.functions.showToast
import com.aubynsamuel.flashsend.home.EditProfileScreen
import com.aubynsamuel.flashsend.home.HomeScreen
import com.aubynsamuel.flashsend.home.ProfileScreen
import com.aubynsamuel.flashsend.home.SearchUsersScreen
import com.aubynsamuel.flashsend.notifications.Notifications
import com.aubynsamuel.flashsend.settings.SettingsRepository
import com.aubynsamuel.flashsend.settings.SettingsScreen
import com.aubynsamuel.flashsend.settings.SettingsViewModel
import com.aubynsamuel.flashsend.settings.SettingsViewModelFactory
import com.aubynsamuel.flashsend.settings.dataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson

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
            // Use Uri.encode to ensure any special characters in the parameters are safely encoded.
            val encodedImageUri = Uri.encode(imageUri)
            val encodedRoomId = Uri.encode(roomId)
            val encodedProfileUrl = Uri.encode(profileUrl)
            val encodedRecipientsToken = Uri.encode(recipientsToken)
            // Convert the boolean to "1" or "0"
            val cameraFlag = if (takenFromCamera) "1" else "0"
            return "imagePreview/$encodedImageUri/$encodedRoomId/$cameraFlag/$encodedProfileUrl/$encodedRecipientsToken"
        }
    }

    object CameraX : Screen("cameraScreen/{roomId}/{profileUrl}/{deviceToken}") {
        fun createRoute(
            roomId: String,
            profileUrl: String,
            deviceToken: String
        ): String {
            val encodedRoomId = Uri.encode(roomId)
            val encodedProfileUrl = Uri.encode(profileUrl)
            val encodedDeviceToken = Uri.encode(deviceToken)
            return "cameraScreen/$encodedRoomId/$encodedProfileUrl/$encodedDeviceToken"
        }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun ChatAppNavigation() {
    val context: Context = LocalContext.current
    val navController = rememberNavController()
    val authViewModelInstance: AuthViewModel = viewModel {
        AuthViewModel(AuthRepository(FirebaseAuth.getInstance()))
    }
    val chatViewModel: ChatViewModel = viewModel {
        ChatViewModel(
            context = context,
        )
    }

    val dataStore = context.applicationContext.dataStore // Changed to application context
    val settingsRepository = SettingsRepository(dataStore)
    val settingsViewModel = viewModel<SettingsViewModel>(
        factory = SettingsViewModelFactory(settingsRepository)
    )
    NavHost(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        navController = navController,
        startDestination = "loadingScreen",
    ) {
        composable("auth") {
            AuthScreen(navController, authViewModelInstance)
        }
        composable("loadingScreen") {
            LoadingScreen(navController, authViewModelInstance)
        }
        composable(
            "home",
            enterTransition = { slideInHorizontally(initialOffsetX = { -it / 2 }) },
        ) {
            HomeScreen(
                navController, authViewModelInstance,
                context = context,
                chatViewModel = chatViewModel
            )
        }
        composable(
            route = Screen.ChatRoom.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it / 2 }) },
            arguments = listOf(navArgument("username") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType },
                navArgument("deviceToken") { type = NavType.StringType },
                navArgument("profileUrl") { type = NavType.StringType })
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val deviceToken = backStackEntry.arguments?.getString("deviceToken") ?: ""
            val profileUrl = backStackEntry.arguments?.getString("profileUrl") ?: ""

            ChatScreen(
                navController = navController,
                username = username,
                userId = userId,
                deviceToken = deviceToken,
                profileUrl = profileUrl,
                settingsViewModel = settingsViewModel,
                authViewModel = authViewModelInstance,
            )
        }
        composable(
            "searchUsers",
            enterTransition = { slideInHorizontally(initialOffsetX = { it / 2 }) }) {
            SearchUsersScreen(
                navController,
            )
        }
        composable(
            "notifications",
            enterTransition = { slideInHorizontally(initialOffsetX = { it / 2 }) }) {
            Notifications(
                context = context,
            )
        }
        composable(
            "setUserDetails",
            enterTransition = { slideInHorizontally(initialOffsetX = { it / 2 }) }) {
            SetUserDetailsScreen(
                navController,
                authViewModel = authViewModelInstance,
            )
        }
        composable(
            "profileScreen",
            enterTransition = { slideInHorizontally(initialOffsetX = { it / 2 }) }) {
            ProfileScreen(
                navController = navController,
                authViewModel = authViewModelInstance
            )
        }
        composable(
            "editProfile",
            enterTransition = { slideInVertically(initialOffsetY = { it / 2 }) }) {
            EditProfileScreen(
                navController = navController,
                authViewModel = authViewModelInstance
            )
        }
        composable(
            "settings",
            enterTransition = { slideInVertically() }) {
            SettingsScreen(
                viewModel = settingsViewModel,
                navController = navController,
                authViewModel = authViewModelInstance
            )
        }
        composable(
            route = "otherProfileScreen/{userJson}",
            arguments = listOf(
                navArgument("userJson") {
                    type = NavType.StringType
                }
            ),
            enterTransition = { slideInVertically() }
        ) { backStackEntry ->
            val userJson = backStackEntry.arguments?.getString("userJson")
            val userData = Gson().fromJson(userJson, User::class.java)
            OtherUserProfile(
                navController = navController,
                userData = userData
            )
        }
        composable(
            route = Screen.ImagePreview.route,
            arguments = listOf(
                navArgument("imageUri") { type = NavType.StringType },
                navArgument("roomId") { type = NavType.StringType },
                navArgument("takenFromCamera") { type = NavType.StringType },
                navArgument("profileUrl") { type = NavType.StringType },
                navArgument("recipientsToken") { type = NavType.StringType }
            ),
            enterTransition = { slideInVertically(initialOffsetY = { it / 2 }) }
        ) { backStackEntry ->
            // Retrieve and validate the arguments
            val imageUriStr = backStackEntry.arguments?.getString("imageUri")
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            val takenFromCameraStr = backStackEntry.arguments?.getString("takenFromCamera") ?: "0"
            val profileUrl = backStackEntry.arguments?.getString("profileUrl") ?: ""
            val recipientsToken = backStackEntry.arguments?.getString("recipientsToken") ?: ""

            if (imageUriStr.isNullOrEmpty()) {
                showToast(context, "An error occurred, Invalid image format")
                return@composable
            }

            val imageUri = imageUriStr.toUri()

            ImagePreviewScreen(
                navController = navController,
                imageUri = imageUri,
                chatViewModel = chatViewModel,
                authViewModel = authViewModelInstance,
                roomId = roomId,
                takenFromCamera = takenFromCameraStr,
                profileUrl = profileUrl,
                recipientsToken = recipientsToken
            )
        }

        composable(
            route = Screen.CameraX.route,
            arguments = listOf(
                navArgument("roomId") { type = NavType.StringType },
                navArgument("profileUrl") { type = NavType.StringType },
                navArgument("deviceToken") { type = NavType.StringType }
            ),
            enterTransition = { slideInVertically(initialOffsetY = { it / 2 }) }
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            val profileUrl = backStackEntry.arguments?.getString("profileUrl") ?: ""
            val deviceToken = backStackEntry.arguments?.getString("deviceToken") ?: ""
            CameraXScreen(
                navController = navController,
                roomId = roomId,
                profileUrl = profileUrl,
                deviceToken = deviceToken,
                onError = { error ->
                    logger("CameraX", error.message.toString())
                },
            )
        }

    }
}
