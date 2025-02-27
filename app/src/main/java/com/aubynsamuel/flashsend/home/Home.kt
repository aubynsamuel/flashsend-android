package com.aubynsamuel.flashsend.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.aubynsamuel.flashsend.R
import com.aubynsamuel.flashsend.auth.AuthViewModel
import com.aubynsamuel.flashsend.chatRoom.ChatViewModel
import com.aubynsamuel.flashsend.chatRoom.DropMenu
import com.aubynsamuel.flashsend.chatRoom.EmptyChatPlaceholder
import com.aubynsamuel.flashsend.chatRoom.PopUpMenu
import com.aubynsamuel.flashsend.functions.ConnectivityStatus
import com.aubynsamuel.flashsend.functions.ConnectivityViewModel
import com.aubynsamuel.flashsend.functions.NetworkConnectivityObserver
import com.aubynsamuel.flashsend.functions.logger
import com.aubynsamuel.flashsend.notifications.NotificationRepository
import com.aubynsamuel.flashsend.notifications.NotificationTokenManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    context: Context,
    chatViewModel: ChatViewModel
) {
    val homeViewModel: HomeViewModel = viewModel {
        HomeViewModel(context)
    }
    val notificationRepository = NotificationRepository()
    val user = FirebaseAuth.getInstance().currentUser
    var retrievedToken by remember { mutableStateOf("") }
    fun getFCMToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("FCM", "Fetching FCM token failed", task.exception)
                    return@addOnCompleteListener
                }
                retrievedToken = task.result
                Log.d("FCM", "FCM Token: $retrievedToken")
            }
    }

    var connectivityViewModel: ConnectivityViewModel = viewModel {
        ConnectivityViewModel(NetworkConnectivityObserver(context))
    }
    val connectivityStatus by connectivityViewModel.connectivityStatus.collectAsStateWithLifecycle()

    val rooms by homeViewModel.rooms.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()

    val authState by authViewModel.authState.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    var netActivity by remember { mutableStateOf("") }

    val permissionRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        onResult = {})
    val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
            getFCMToken()
        } else {
            netActivity = "Connecting..."
        }
    }
    LaunchedEffect(Unit) {
        if (!hasNotificationPermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        authViewModel.loadUserData()
        try {
            notificationRepository.checkServerHealth()
        } catch (e: Exception) {
            logger("NetWorkError", e.message.toString())
        }
    }
    LaunchedEffect(retrievedToken) {
        try {
            if (user != null) {
                NotificationTokenManager.initializeAndUpdateToken(
                    context, user.uid, retrievedToken.toString()
                )
            } else {
                Log.w("NotificationTokenChange", "User not signed in; cannot update token.")
            }
        } catch (e: Exception) {
            logger("NetWorkError", e.message.toString())
        }
        try {
            getFCMToken()
            notificationRepository.checkServerHealth()
        } catch (e: Exception) {
            logger("NetWorkError", e.message.toString())
        }
    }
    LaunchedEffect(retrievedToken) {
        try {
            if (user != null) {
                NotificationTokenManager.initializeAndUpdateToken(
                    context, user.uid, retrievedToken.toString()
                )
            } else {
                Log.w("NotificationTokenChange", "User not signed in; cannot update token.")
            }
        } catch (e: Exception) {
            logger("NetWorkError", e.message.toString())
        }
    }

    LaunchedEffect(authState) {
        if (!authState) {
            navController.navigate("auth") {
                popUpTo("main?initialPage=0") { inclusive = true }
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
            Row {
                Text(
                    "Flash Send",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )
                if (netActivity.isNotEmpty()) {
                    Text(
                        text = if (isLoading) "Loading..." else netActivity,
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
                        .clickable(onClick = { navController.navigate("searchUsers") })
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
                                navController.navigate("main?initialPage=1") {
                                    popUpTo("main?initialPage=0") { inclusive = true }
                                }
                            },
                            icon = Icons.Default.Person
                        ),
                        DropMenu(
                            text = "Settings",
                            onClick = {
                                navController.navigate("main?initialPage=2") {
                                    popUpTo("main?initialPage=0") { inclusive = true }
                                }
                            },
                            icon = Icons.Default.Settings
                        ),
                        DropMenu(
                            text = "QRScannerScreen",
                            onClick = { navController.navigate("QRScannerScreen") },
                            icon = Icons.Default.QrCodeScanner
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
            onClick = { navController.navigate("searchUsers") },
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
            if (rooms.isEmpty() && !isLoading) {
                EmptyChatPlaceholder(
                    lottieAnimation = R.raw.online_chat,
                    message = "Press + to search users",
                    speed = 0.6f,
                    color = MaterialTheme.colorScheme.onBackground
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                ) {
                    items(rooms) { room ->
                        ChatListItem(
                            room, navController,
                            chatViewModel = chatViewModel
                        )
                    }
                }
            }
        }
    }
}