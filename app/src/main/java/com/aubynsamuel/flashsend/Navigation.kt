package com.aubynsamuel.flashsend

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
import com.aubynsamuel.flashsend.chatRoom.ChatScreen
import com.aubynsamuel.flashsend.chatRoom.ChatViewModel
import com.aubynsamuel.flashsend.chatRoom.ImagePreviewScreen
import com.aubynsamuel.flashsend.chatRoom.OtherUserProfile
import com.aubynsamuel.flashsend.functions.User
import com.aubynsamuel.flashsend.home.EditProfileScreen
import com.aubynsamuel.flashsend.home.HomeScreen
import com.aubynsamuel.flashsend.home.ProfileScreen
import com.aubynsamuel.flashsend.home.SearchUsersScreen
import com.aubynsamuel.flashsend.settings.SettingsRepository
import com.aubynsamuel.flashsend.settings.SettingsScreen
import com.aubynsamuel.flashsend.settings.SettingsViewModel
import com.aubynsamuel.flashsend.settings.SettingsViewModelFactory
import com.aubynsamuel.flashsend.settings.dataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson

sealed class Screen(val route: String) {
    object ChatRoom : Screen("chatRoom/{username}/{userId}/{deviceToken}/{profileUrl}/{roomId}") {
        fun createRoute(
            username: String,
            userId: String,
            deviceToken: String,
            profileUrl: String,
            roomId: String
        ) = "chatRoom/$username/$userId/$deviceToken/$profileUrl/${roomId}"
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
        ChatViewModel(context)
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
                context = context
            )
        }
        composable(
            route = Screen.ChatRoom.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it / 2 }) },
            arguments = listOf(navArgument("username") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType },
                navArgument("deviceToken") { type = NavType.StringType },
                navArgument("profileUrl") { type = NavType.StringType },
                navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val deviceToken = backStackEntry.arguments?.getString("deviceToken") ?: ""
            val profileUrl = backStackEntry.arguments?.getString("profileUrl") ?: ""
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""

            ChatScreen(
                navController = navController,
                username = username,
                userId = userId,
                deviceToken = deviceToken,
                profileUrl = profileUrl,
                roomId = roomId,
                settingsViewModel = settingsViewModel,
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
            route = "imagePreview/{imageUri}/{roomId}",
            arguments = listOf(
                navArgument("imageUri") { type = NavType.StringType },
                navArgument("roomId") { type = NavType.StringType }
            ),
            enterTransition = { slideInVertically(initialOffsetY = { it / 2 }) }
        ) { backStackEntry ->
            // Retrieve the imageUri from the arguments
            val imageUri = backStackEntry.arguments?.getString("imageUri")
            val roomId = backStackEntry.arguments?.getString("roomId")

            // Only show the screen if imageUri is not null
            imageUri?.let {
                ImagePreviewScreen(
                    navController = navController,
                    imageUri = it.toUri(),
                    chatViewModel = chatViewModel,
                    authViewModel = authViewModelInstance,
                    roomId = roomId.toString()
                )
            }
        }
    }
}
