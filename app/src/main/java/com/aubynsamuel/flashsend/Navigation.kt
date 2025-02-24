package com.aubynsamuel.flashsend

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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
import com.aubynsamuel.flashsend.chatRoom.QRScannerScreen
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
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.launch

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

data class BottomNavItem(val route: String, val icon: ImageVector, val label: String)

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
                    authViewModel = authViewModelInstance,
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
            enterTransition = { slideInVertically() }) { backStackEntry ->
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
            enterTransition = { slideInVertically(initialOffsetY = { it / 2 }) }) { backStackEntry ->
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
                authViewModel = authViewModelInstance,
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
            enterTransition = { slideInVertically(initialOffsetY = { it / 2 }) }) { backStackEntry ->
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


@OptIn(ExperimentalPagerApi::class)
@Composable
fun MainBottomNavScreen(
    navController: NavController,
    authViewModelInstance: AuthViewModel,
    chatViewModel: ChatViewModel,
    settingsViewModel: SettingsViewModel,
    context: Context,
    initialPage: Int = 0

) {
    val bottomNavItems = listOf(
        BottomNavItem("home", Icons.AutoMirrored.Default.Chat, "Chats"),
        BottomNavItem("profileScreen", Icons.Default.Person, "Profile"),
        BottomNavItem("settings", Icons.Default.Settings, "Settings")
    )

    val pagerState = rememberPagerState(initialPage = initialPage)
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            count = bottomNavItems.size,
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) { page ->
            when (page) {
                0 -> HomeScreen(
                    navController = navController,
                    context = context,
                    chatViewModel = chatViewModel,
                    authViewModel = authViewModelInstance
                )

                1 -> ProfileScreen(
                    navController = navController,
                    authViewModel = authViewModelInstance
                )

                2 -> SettingsScreen(
                    viewModel = settingsViewModel,
                    navController = navController,
                    authViewModel = authViewModelInstance
                )
            }
        }

        NavigationBar(
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxWidth()
                .drawWithContent {
                    drawContent()
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1f
                    )
                }
        ) {
            bottomNavItems.forEachIndexed { index, item ->
                NavigationBarItem(
                    icon = {
                        BadgedBox(
                            badge = {
                                if (item.route == "home" && chatViewModel.unreadRoomIds.isNotEmpty()) {
                                    Badge(
                                        contentColor = Color.White,
                                        containerColor = Color.Red
                                    ) { Text(chatViewModel.unreadRoomIds.size.toString()) }
                                }
                            }
                        ) {
                            Icon(item.icon, contentDescription = item.label)
                        }
                    },
                    label = { Text(item.label, fontWeight = FontWeight.SemiBold) },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.scrollToPage(index)
                        }
                    },
                    colors = NavigationBarItemColors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedIndicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = NavigationBarItemDefaults.colors().unselectedIconColor,
                        unselectedTextColor = NavigationBarItemDefaults.colors().unselectedTextColor,
                        disabledIconColor = NavigationBarItemDefaults.colors().disabledIconColor,
                        disabledTextColor = NavigationBarItemDefaults.colors().disabledTextColor
                    )
                )
            }
        }
    }
}