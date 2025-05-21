package com.aubynsamuel.flashsend.navigation

import android.content.Context
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aubynsamuel.flashsend.LoadingScreen
import com.aubynsamuel.flashsend.auth.presentation.screens.AuthScreen
import com.aubynsamuel.flashsend.auth.presentation.screens.SetUserDetailsScreen
import com.aubynsamuel.flashsend.auth.presentation.viewmodels.AuthViewModel
import com.aubynsamuel.flashsend.chatRoom.presentation.screens.CameraXScreen
import com.aubynsamuel.flashsend.chatRoom.presentation.screens.ChatScreen
import com.aubynsamuel.flashsend.chatRoom.presentation.screens.ImagePreviewScreen
import com.aubynsamuel.flashsend.chatRoom.presentation.screens.OtherUserProfileScreen
import com.aubynsamuel.flashsend.chatRoom.presentation.screens.QRScannerScreen
import com.aubynsamuel.flashsend.chatRoom.presentation.viewmodels.ChatViewModel
import com.aubynsamuel.flashsend.core.domain.dataStore
import com.aubynsamuel.flashsend.core.domain.logger
import com.aubynsamuel.flashsend.core.domain.showToast
import com.aubynsamuel.flashsend.core.model.User
import com.aubynsamuel.flashsend.home.presentation.screens.EditProfileScreen
import com.aubynsamuel.flashsend.home.presentation.screens.SearchUsersScreen
import com.aubynsamuel.flashsend.notifications.presentation.NotificationTestScreen
import com.aubynsamuel.flashsend.settings.data.SettingsRepository
import com.aubynsamuel.flashsend.settings.domain.SettingsViewModelFactory
import com.aubynsamuel.flashsend.settings.presentation.viewmodels.SettingsViewModel
import com.google.gson.Gson

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun ChatAppNavigation() {
    val context: Context = LocalContext.current
    val navController = rememberNavController()
    val tag = "Navigation"

    val authViewModelInstance: AuthViewModel = hiltViewModel()
    val chatViewModel: ChatViewModel = hiltViewModel()
    val dataStore = context.applicationContext.dataStore
    val settingsRepository = SettingsRepository(dataStore)
    val settingsViewModel = viewModel<SettingsViewModel>(
        factory = SettingsViewModelFactory(settingsRepository)
    )

    NavHost(
        navController = navController,
        startDestination = "loadingScreen",
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        composable("auth") {
            AuthScreen(navController, authViewModelInstance)
        }
        composable("loadingScreen") {
            LoadingScreen(navController, authViewModelInstance)
        }
        composable(
            route = "main?initialPage={initialPage}",
            arguments = listOf(navArgument("initialPage") {
                type = NavType.IntType
                defaultValue = 0
            })
        ) { backStackEntry ->
            val initialPage = backStackEntry.arguments?.getInt("initialPage") ?: 0
            MainBottomNavScreen(
                navController = navController,
                authViewModelInstance = authViewModelInstance,
                chatViewModel = chatViewModel,
                settingsViewModel = settingsViewModel,
                context = context,
                initialPage = initialPage
            )
        }
        composable(
            route = Screen.ChatRoom.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it / 2 }) },
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
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
                chatViewModel = chatViewModel,
            )
        }
        composable(
            "searchUsers",
            enterTransition = { slideInHorizontally(initialOffsetX = { it / 2 }) }) {
            SearchUsersScreen(navController)
        }
        composable(
            "notifications",
            enterTransition = { slideInHorizontally(initialOffsetX = { it / 2 }) }) {
            NotificationTestScreen(context = context)
        }
        composable(
            "QRScannerScreen",
            enterTransition = { slideInHorizontally(initialOffsetX = { it / 2 }) }) {
            QRScannerScreen()
        }
        composable(
            "setUserDetails",
            enterTransition = { slideInHorizontally(initialOffsetX = { it / 2 }) }) {
            SetUserDetailsScreen(navController, authViewModel = authViewModelInstance)
        }
        composable(
            "editProfile",
            enterTransition = { slideInVertically(initialOffsetY = { it / 2 }) }) {
            EditProfileScreen(navController, authViewModel = authViewModelInstance)
        }
        composable(
            route = "otherProfileScreen/{userJson}",
            arguments = listOf(navArgument("userJson") { type = NavType.StringType }),
            enterTransition = { slideInVertically(initialOffsetY = { it / 2 }) })
        { backStackEntry ->
            val userJson = backStackEntry.arguments?.getString("userJson")
            val userData = Gson().fromJson(userJson, User::class.java)
            OtherUserProfileScreen(navController = navController, userData = userData)
        }
        composable(
            route = Screen.ImagePreview.route,
            arguments = listOf(
                navArgument("imageUri") { type = NavType.StringType },
                navArgument("roomId") { type = NavType.StringType },
                navArgument("takenFromCamera") { type = NavType.StringType },
                navArgument("profileUrl") { type = NavType.StringType },
                navArgument("recipientsToken") { type = NavType.StringType }),
            enterTransition = { slideInVertically(initialOffsetY = { it / 2 }) })
        { backStackEntry ->
            val imageUriStr = backStackEntry.arguments?.getString("imageUri")
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            val takenFromCameraStr =
                backStackEntry.arguments?.getString("takenFromCamera") ?: "0"
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
                navArgument("deviceToken") { type = NavType.StringType }),
            enterTransition = { slideInVertically(initialOffsetY = { it / 2 }) })
        { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            val profileUrl = backStackEntry.arguments?.getString("profileUrl") ?: ""
            val deviceToken = backStackEntry.arguments?.getString("deviceToken") ?: ""
            CameraXScreen(
                navController = navController,
                roomId = roomId,
                profileUrl = profileUrl,
                deviceToken = deviceToken,
                onError = { error ->
                    logger(tag, error.message.toString())
                })
        }
    }
}