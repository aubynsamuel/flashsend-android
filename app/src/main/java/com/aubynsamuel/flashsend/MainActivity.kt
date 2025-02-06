package com.aubynsamuel.flashsend

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aubynsamuel.flashsend.auth.AuthRepository
import com.aubynsamuel.flashsend.auth.AuthScreen
import com.aubynsamuel.flashsend.auth.AuthViewModel
import com.aubynsamuel.flashsend.chatRoom.ChatScreen
import com.aubynsamuel.flashsend.home.HomeScreen
import com.aubynsamuel.flashsend.home.HomeViewModel
import com.aubynsamuel.flashsend.home.SearchUsersScreen
import com.aubynsamuel.flashsend.ui.theme.FlashSendTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            FlashSendTheme {
                ChatAppNavigation()
            }
        }
    }
}

sealed class Screen(val route: String) {
    object ChatRoom : Screen("chatRoom/{username}/{userId}/{deviceToken}/{profileUrl}/{roomId}") {
        fun createRoute(
            username: String,
            userId: String,
            deviceToken: String,
            profileUrl: String,
            roomId: String
        ) =
            "chatRoom/$username/$userId/$deviceToken/$profileUrl/${roomId}"
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun ChatAppNavigation() {
    val navController = rememberNavController()
    val authViewModelInstance: AuthViewModel = viewModel {
        AuthViewModel(AuthRepository(FirebaseAuth.getInstance()))
    }
    val homeViewModelInstance: HomeViewModel = viewModel()

    NavHost(navController = navController, startDestination = "auth") {
        composable("auth") {
            AuthScreen(
                navController, authViewModelInstance
            )
        }
//        composable("signup") { SignupScreen(navController) }
        composable("home") {
            HomeScreen(
                navController,
                homeViewModelInstance,
                authViewModelInstance
            )
        }
        composable(
            route = Screen.ChatRoom.route,
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType },
                navArgument("deviceToken") { type = NavType.StringType },
                navArgument("profileUrl") { type = NavType.StringType },
                navArgument("roomId") { type = NavType.StringType }
            )
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
                roomId = roomId
            )
        }
        composable("searchUsers") { SearchUsersScreen(navController) }
//        composable("settings") { SettingsScreen(navController) }
    }
}