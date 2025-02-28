package com.aubynsamuel.flashsend.navigation

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aubynsamuel.flashsend.LoadingScreen
import com.aubynsamuel.flashsend.auth.AuthRepository
import com.aubynsamuel.flashsend.auth.AuthScreen
import com.aubynsamuel.flashsend.auth.AuthViewModel
import com.aubynsamuel.flashsend.auth.SetUserDetailsScreen
import com.aubynsamuel.flashsend.chatRoom.CameraXScreen
import com.aubynsamuel.flashsend.chatRoom.ChatScreen
import com.aubynsamuel.flashsend.chatRoom.ChatViewModel
import com.aubynsamuel.flashsend.chatRoom.ImagePreviewScreen
import com.aubynsamuel.flashsend.chatRoom.OtherUserProfile
import com.aubynsamuel.flashsend.chatRoom.QRScannerScreen
import com.aubynsamuel.flashsend.functions.User
import com.aubynsamuel.flashsend.functions.logger
import com.aubynsamuel.flashsend.functions.showToast
import com.aubynsamuel.flashsend.home.EditProfileScreen
import com.aubynsamuel.flashsend.home.SearchUsersScreen
import com.aubynsamuel.flashsend.notifications.Notifications
import com.aubynsamuel.flashsend.settings.SettingsRepository
import com.aubynsamuel.flashsend.settings.SettingsViewModel
import com.aubynsamuel.flashsend.settings.SettingsViewModelFactory
import com.aubynsamuel.flashsend.settings.dataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun ChatAppNavigation() {
    val context: Context = LocalContext.current
    val navController = rememberNavController()

    val authViewModelInstance: AuthViewModel = viewModel {
        AuthViewModel(
            repository = AuthRepository(FirebaseAuth.getInstance()),
            context = context
        )
    }
    val chatViewModel: ChatViewModel = viewModel {
        ChatViewModel(context = context)
    }
    val dataStore = context.applicationContext.dataStore
    val settingsRepository = SettingsRepository(dataStore)
    val settingsViewModel = viewModel<SettingsViewModel>(
        factory = SettingsViewModelFactory(settingsRepository)
    )

    val saveableStateHolder = rememberSaveableStateHolder()

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
            MainBottomNavScreen(
                navController = navController,
                authViewModelInstance = authViewModelInstance,
                chatViewModel = chatViewModel,
                settingsViewModel = settingsViewModel,
                context = context,
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
            saveableStateHolder.SaveableStateProvider(Screen.ChatRoom.route) {
                ChatScreen(
                    navController = navController,
                    username = username,
                    userId = userId,
                    deviceToken = deviceToken,
                    profileUrl = profileUrl,
                    settingsViewModel = settingsViewModel,
                )
            }
        }
        composable(
            "searchUsers",
            enterTransition = { slideInHorizontally(initialOffsetX = { it / 2 }) }) {
            SearchUsersScreen(navController)
        }
        composable(
            "notifications",
            enterTransition = { slideInHorizontally(initialOffsetX = { it / 2 }) }) {
            Notifications(context = context)
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
        composable(route = "otherProfileScreen/{userJson}",
            arguments = listOf(navArgument("userJson") { type = NavType.StringType }),
            enterTransition = { slideInVertically(initialOffsetY = { it / 2 }) })
        { backStackEntry ->
            val userJson = backStackEntry.arguments?.getString("userJson")
            val userData = Gson().fromJson(userJson, User::class.java)
            OtherUserProfile(navController = navController, userData = userData)
        }
        composable(route = Screen.ImagePreview.route,
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType },
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
            val imageUri = Uri.parse(imageUriStr)
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
        composable(route = Screen.CameraX.route,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType },
                navArgument("profileUrl") { type = NavType.StringType },
                navArgument("deviceToken") { type = NavType.StringType }),
            enterTransition = { slideInVertically(initialOffsetY = { it / 2 }) })
        { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            val profileUrl = backStackEntry.arguments?.getString("profileUrl") ?: ""
            val deviceToken = backStackEntry.arguments?.getString("deviceToken") ?: ""
            CameraXScreen(navController = navController,
                roomId = roomId,
                profileUrl = profileUrl,
                deviceToken = deviceToken,
                onError = { error ->
                    logger("CameraX", error.message.toString())
                })
        }
    }
}