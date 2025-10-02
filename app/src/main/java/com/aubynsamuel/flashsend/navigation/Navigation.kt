package com.aubynsamuel.flashsend.navigation

import android.content.Context
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.aubynsamuel.flashsend.LoadingScreen
import com.aubynsamuel.flashsend.auth.presentation.screens.AuthScreen
import com.aubynsamuel.flashsend.auth.presentation.screens.SetUserDetailsScreen
import com.aubynsamuel.flashsend.auth.presentation.viewmodels.AuthViewModel
import com.aubynsamuel.flashsend.chat.presentation.components.FullScreenImageViewer
import com.aubynsamuel.flashsend.chat.presentation.screens.CameraXScreen
import com.aubynsamuel.flashsend.chat.presentation.screens.ChatScreen
import com.aubynsamuel.flashsend.chat.presentation.screens.ImagePreviewScreen
import com.aubynsamuel.flashsend.chat.presentation.screens.OtherUserProfileScreen
import com.aubynsamuel.flashsend.chat.presentation.viewmodels.ChatViewModel
import com.aubynsamuel.flashsend.core.domain.model.User
import com.aubynsamuel.flashsend.core.presentation.navigation.AuthScreenDC
import com.aubynsamuel.flashsend.core.presentation.navigation.CameraXScreenDC
import com.aubynsamuel.flashsend.core.presentation.navigation.ChatRoomScreen
import com.aubynsamuel.flashsend.core.presentation.navigation.EditProfileDC
import com.aubynsamuel.flashsend.core.presentation.navigation.FullScreenImageViewerDC
import com.aubynsamuel.flashsend.core.presentation.navigation.ImagePreviewScreen
import com.aubynsamuel.flashsend.core.presentation.navigation.LoadingScreenDC
import com.aubynsamuel.flashsend.core.presentation.navigation.MainScreen
import com.aubynsamuel.flashsend.core.presentation.navigation.OtherProfileScreenDC
import com.aubynsamuel.flashsend.core.presentation.navigation.SearchUsersScreenDC
import com.aubynsamuel.flashsend.core.presentation.navigation.SetUserDetailsDC
import com.aubynsamuel.flashsend.core.presentation.utils.logger
import com.aubynsamuel.flashsend.core.presentation.utils.showToast
import com.aubynsamuel.flashsend.home.presentation.screens.EditProfileScreen
import com.aubynsamuel.flashsend.home.presentation.screens.SearchUsersScreen
import com.aubynsamuel.flashsend.settings.presentation.viewmodels.SettingsViewModel
import com.google.gson.Gson

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope =
    compositionLocalOf<SharedTransitionScope> { error("No SharedTransitionScope found") }
val LocalNavController = compositionLocalOf<NavController> { error("NavController is required") }
val LocalChatRoomAnimatedVisibilityScope =
    compositionLocalOf<AnimatedVisibilityScope> { error("No AnimatedVisibilityScope found") }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ChatAppNavigation() {
    val context: Context = LocalContext.current
    val navController = rememberNavController()
    val tag = "Navigation"

    val authViewModelInstance: AuthViewModel = hiltViewModel()
    val chatViewModel: ChatViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    SharedTransitionLayout {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this@SharedTransitionLayout,
            LocalNavController provides navController
        ) {
            NavHost(
                navController = navController,
                startDestination = LoadingScreenDC,
                modifier = Modifier.background(MaterialTheme.colorScheme.background),

                enterTransition = { EnterTransition.Companion.None },
                exitTransition = { ExitTransition.Companion.None },
                popEnterTransition = { EnterTransition.Companion.None },
                popExitTransition = { ExitTransition.Companion.None }
            ) {
                composable<AuthScreenDC> {
                    AuthScreen(navController, authViewModelInstance)
                }

                composable<LoadingScreenDC> {
                    LoadingScreen(navController, authViewModelInstance)
                }

                composable<MainScreen> {
                    val args = it.toRoute<MainScreen>()
                    MainBottomNavScreen(
                        navController = navController,
                        authViewModelInstance = authViewModelInstance,
                        settingsViewModel = settingsViewModel,
                        context = context,
                        initialPage = args.initialPage,
                        animatedScope = this,
                    )
                }

                composable<ChatRoomScreen>(
                    enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                    popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                    popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                ) {
                    val args = it.toRoute<ChatRoomScreen>()
                    CompositionLocalProvider(LocalChatRoomAnimatedVisibilityScope provides this) {
                        ChatScreen(
                            navController = navController,
                            username = args.username,
                            userId = args.userId,
                            deviceToken = args.deviceToken,
                            profileUrl = args.profileUrl,
                            settingsViewModel = settingsViewModel,
                        )
                    }
                }

                composable<SearchUsersScreenDC>(
                    enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                    popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                    popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                ) {
                    SearchUsersScreen(navController)
                }
                composable<SetUserDetailsDC> {
                    SetUserDetailsScreen(navController, authViewModel = authViewModelInstance)
                }

                composable<EditProfileDC>(
                    enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                    popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                    popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                ) {
                    EditProfileScreen(navController, authViewModel = authViewModelInstance)
                }

                composable<OtherProfileScreenDC> {
                    val args = it.toRoute<OtherProfileScreenDC>()
                    val userData = Gson().fromJson(args.user, User::class.java)
                    OtherUserProfileScreen(
                        navController = navController,
                        userData = userData,
                        animatedScope = this
                    )
                }

                composable<ImagePreviewScreen> {
                    val args = it.toRoute<ImagePreviewScreen>()
                    if (args.imageUri.isEmpty()) {
                        showToast(context, "An error occurred, Invalid image format")
                        return@composable
                    }
                    ImagePreviewScreen(
                        navController = navController,
                        chatViewModel = chatViewModel,
                        imageUri = args.imageUri.toUri(),
                        takenFromCamera = args.takenFromCamera.toString(),
                        recipientsToken = args.recipientsToken
                    )
                }

                composable<CameraXScreenDC> {
                    val args = it.toRoute<CameraXScreenDC>()
                    CameraXScreen(
                        navController = navController,
                        deviceToken = args.deviceToken,
                        onError = { error ->
                            logger(tag, error.message.toString())
                        }
                    )
                }
                composable<FullScreenImageViewerDC> {
                    val args = it.toRoute<FullScreenImageViewerDC>()
                    FullScreenImageViewer(
                        imageUri = args.imageUri,
                        onDismiss = { navController.safePopBackStack() },
                        animatedScope = this,
                    )
                }
            }
        }
    }
}