package com.aubynsamuel.flashsend.home.presentation.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.aubynsamuel.flashsend.R
import com.aubynsamuel.flashsend.auth.presentation.viewmodels.AuthViewModel
import com.aubynsamuel.flashsend.chat.presentation.components.EmptyChatPlaceholder
import com.aubynsamuel.flashsend.core.data.ConnectivityStatus
import com.aubynsamuel.flashsend.core.domain.model.DropMenu
import com.aubynsamuel.flashsend.core.presentation.components.PopUpMenu
import com.aubynsamuel.flashsend.core.presentation.navigation.AuthScreenDC
import com.aubynsamuel.flashsend.core.presentation.navigation.MainScreen
import com.aubynsamuel.flashsend.core.presentation.navigation.SearchUsersScreenDC
import com.aubynsamuel.flashsend.core.presentation.utils.logger
import com.aubynsamuel.flashsend.core.presentation.viewModels.ConnectivityViewModel
import com.aubynsamuel.flashsend.home.presentation.components.ChatListItem
import com.aubynsamuel.flashsend.home.presentation.viewmodels.HomeViewModel
import com.aubynsamuel.flashsend.notifications.data.NotificationTokenManager
import com.aubynsamuel.flashsend.notifications.data.api.ApiRequestsRepository
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    context: Context,
    animatedScope: AnimatedVisibilityScope,
    homeViewModel: HomeViewModel,
) {
    val notificationRepository = ApiRequestsRepository()
    val user = FirebaseAuth.getInstance().currentUser
    val notificationTokenManager = NotificationTokenManager()
    val tag = "homeLogs"

    fun updateFCMToken() {
        homeViewModel.getFCMToken { token ->
            try {
                if (user != null) {
                    notificationTokenManager.initializeAndUpdateToken(
                        context, user.uid, token
                    )
                } else {
                    Log.w(tag, "User not signed in; cannot update token.")
                }
            } catch (e: Exception) {
                logger(tag, e.message.toString())
            }
        }
    }

    val connectivityViewModel: ConnectivityViewModel = hiltViewModel()
    val connectivityStatus by connectivityViewModel.connectivityStatus.collectAsStateWithLifecycle()

    val homeUiState by homeViewModel.uiState.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    var netActivity by remember { mutableStateOf("") }

    val permissionRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        onResult = {})
    val hasNotificationPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

    LaunchedEffect(connectivityStatus) {
        if (connectivityStatus is ConnectivityStatus.Available) {
            netActivity = ""
            homeViewModel.retryLoadRooms()
        } else {
            netActivity = "Connecting..."
        }
    }

    LaunchedEffect(Unit) {
        updateFCMToken()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission) {
                permissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        authViewModel.loadUserData()
        try {
            notificationRepository.checkServerHealth()
        } catch (e: Exception) {
            logger(tag, e.message.toString())
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            navController.navigate(AuthScreenDC) {
                popUpTo(MainScreen(0)) { inclusive = true }
            }
        }
    }

    Scaffold(topBar = {
        Row(
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth(1f)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(top = 15.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f)) {
                Text(
                    "Flash Send",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                if (netActivity.isNotBlank()) {
                    Text(
                        text = if (homeUiState.isLoading) "Loading..." else netActivity,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 10.dp, top = 3.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Row {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .clickable(onClick = { navController.navigate(SearchUsersScreenDC) })
                        .padding(end = 5.dp)
                )
                Icon(
                    Icons.Outlined.MoreVert,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .clickable(onClick = { expanded = true })
                        .padding(horizontal = 5.dp)
                )
                PopUpMenu(
                    expanded = expanded,
                    onDismiss = { expanded = !expanded },
                    modifier = Modifier,
                    dropItems = listOf(
                        DropMenu(
                            text = "Profile",
                            onClick = {
                                navController.navigate(MainScreen(1)) {
                                    popUpTo(MainScreen(0)) { inclusive = false }
                                }
                            },
                            icon = Icons.Default.Person
                        ),
                        DropMenu(
                            text = "Settings",
                            onClick = {
                                navController.navigate(MainScreen(2)) {
                                    popUpTo(MainScreen(0)) { inclusive = false }
                                }
                            },
                            icon = Icons.Default.Settings
                        ),
                        DropMenu(
                            text = "Logout",
                            onClick = { authViewModel.logout() },
                            icon = Icons.AutoMirrored.Default.Logout
                        ),
                    ),
                    reactions = {}
                )
            }
        }
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = { navController.navigate(SearchUsersScreenDC) },
            modifier = Modifier.padding(bottom = 20.dp, end = 5.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Chat")
        }
    }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (homeUiState.rooms.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                ) {
                    items(homeUiState.rooms) { room ->
                        if (room?.otherParticipant != null)
                            ChatListItem(
                                room = room,
                                navController = navController,
                                homeViewModel = homeViewModel,
                                animatedScope = animatedScope,
                            )
                    }
                }
            } else if (homeUiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            } else {
                EmptyChatPlaceholder(
                    lottieAnimation = R.raw.online_chat,
                    message = "Press + to search users",
                    speed = 0.6f,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}