package com.aubynsamuel.flashsend.navigation

import android.content.Context
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.aubynsamuel.flashsend.LoadingScreen
import com.aubynsamuel.flashsend.auth.presentation.screens.AuthScreen
import com.aubynsamuel.flashsend.auth.presentation.screens.SetUserDetailsScreen
import com.aubynsamuel.flashsend.auth.presentation.viewmodels.AuthViewModel
import com.aubynsamuel.flashsend.chatRoom.presentation.screens.CameraXScreen
import com.aubynsamuel.flashsend.chatRoom.presentation.screens.ChatScreen
import com.aubynsamuel.flashsend.chatRoom.presentation.screens.ImagePreviewScreen
import com.aubynsamuel.flashsend.chatRoom.presentation.screens.OtherUserProfileScreen
import com.aubynsamuel.flashsend.chatRoom.presentation.viewmodels.ChatViewModel
import com.aubynsamuel.flashsend.core.domain.logger
import com.aubynsamuel.flashsend.core.domain.showToast
import com.aubynsamuel.flashsend.core.model.User
import com.aubynsamuel.flashsend.home.presentation.screens.EditProfileScreen
import com.aubynsamuel.flashsend.home.presentation.screens.SearchUsersScreen
import com.aubynsamuel.flashsend.settings.presentation.viewmodels.SettingsViewModel
import com.google.gson.Gson

@Composable
fun ChatAppNavigation() {
    val context: Context = LocalContext.current
    val navController = rememberNavController()
    val tag = "Navigation"

    val authViewModelInstance: AuthViewModel = hiltViewModel()
    val chatViewModel: ChatViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = LoadingScreen,
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        composable<AuthScreen> {
            AuthScreen(navController, authViewModelInstance)
        }

        composable<LoadingScreen> {
            LoadingScreen(navController, authViewModelInstance)
        }

        composable<MainScreen> {
            val args = it.toRoute<MainScreen>()
            MainBottomNavScreen(
                navController = navController,
                authViewModelInstance = authViewModelInstance,
                chatViewModel = chatViewModel,
                settingsViewModel = settingsViewModel,
                context = context,
                initialPage = args.initialPage
            )
        }

        composable<ChatRoomScreen>(
            enterTransition = { slideInHorizontally(initialOffsetX = { it / 2 }) }
        ) {
            val args = it.toRoute<ChatRoomScreen>()
            ChatScreen(
                navController = navController,
                username = args.username,
                userId = args.userId,
                deviceToken = args.deviceToken,
                profileUrl = args.profileUrl,
                settingsViewModel = settingsViewModel,
            )
        }

        composable<SearchUsersScreenDC>(
            enterTransition = { slideInHorizontally(initialOffsetX = { it / 2 }) }) {
            SearchUsersScreen(navController)
        }

//        composable(
//            "notifications",
//            enterTransition = { slideInHorizontally(initialOffsetX = { it / 2 }) }) {
//            NotificationTestScreen(context = context)
//        }

        composable<SetUserDetailsDC>(
            enterTransition = { slideInHorizontally(initialOffsetX = { it / 2 }) }) {
            SetUserDetailsScreen(navController, authViewModel = authViewModelInstance)
        }

        composable<EditProfileDC>(
            enterTransition = { slideInVertically(initialOffsetY = { it / 2 }) }) {
            EditProfileScreen(navController, authViewModel = authViewModelInstance)
        }

        composable<OtherProfileScreenDC>(
            enterTransition = { slideInVertically(initialOffsetY = { it / 2 }) }
        ) {
            val args = it.toRoute<OtherProfileScreenDC>()
            val userData = Gson().fromJson(args.user, User::class.java)
            OtherUserProfileScreen(navController = navController, userData = userData)
        }

        composable<ImagePreviewScreen>(
            enterTransition = { slideInVertically(initialOffsetY = { it / 2 }) }
        ) {
            val args = it.toRoute<ImagePreviewScreen>()
            if (args.imageUri.isEmpty()) {
                showToast(context, "An error occurred, Invalid image format")
                return@composable
            }
            ImagePreviewScreen(
                navController = navController,
                chatViewModel = chatViewModel,
                imageUri = args.imageUri.toUri(),
                roomId = args.roomId,
                takenFromCamera = args.takenFromCamera.toString(),
                profileUrl = args.profileUrl,
                recipientsToken = args.recipientsToken
            )
        }

        composable<CameraXScreenDC>(
            enterTransition = { slideInVertically(initialOffsetY = { it / 2 }) }
        ) {
            val args = it.toRoute<CameraXScreenDC>()
            CameraXScreen(
                navController = navController,
                roomId = args.roomId,
                profileUrl = args.profileUrl,
                deviceToken = args.deviceToken,
                onError = { error ->
                    logger(tag, error.message.toString())
                }
            )
        }
    }
}