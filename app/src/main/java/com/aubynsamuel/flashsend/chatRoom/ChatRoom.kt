package com.aubynsamuel.flashsend.chatRoom

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.aubynsamuel.flashsend.R
import com.aubynsamuel.flashsend.functions.ConnectivityStatus
import com.aubynsamuel.flashsend.functions.NetworkConnectivityObserver
import com.aubynsamuel.flashsend.functions.User
import com.aubynsamuel.flashsend.settings.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.net.URLDecoder


@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun ChatScreen(
    navController: NavController,
    username: String,
    userId: String,
    deviceToken: String,
    profileUrl: String,
    roomId: String,
    settingsViewModel: SettingsViewModel
) {
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: return
    val roomId by remember { mutableStateOf(createRoomId(userId, currentUserId)) }
    val context = LocalContext.current
    val chatViewModel: ChatViewModel = viewModel {
        ChatViewModel(context)
    }
    val fontSize by settingsViewModel.uiState.collectAsState()
    val chatState by chatViewModel.chatState.collectAsState()
    val messages by chatViewModel.messages.collectAsState()
//    val messages = generateMockMessages(currentUserId)
    val decodedUsername = URLDecoder.decode(username, "UTF-8")
    var messageText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val listState = rememberLazyListState()

    val showScrollToBottom by remember {
        derivedStateOf {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            firstVisibleIndex - 1 > 0
        }
    }
    val connectivityObserver = remember { NetworkConnectivityObserver(context) }
    // Collect network connectivity state
    val networkStatus by connectivityObserver.observe().collectAsState(
        initial = ConnectivityStatus.Unavailable
    )
    var netActivity by remember { mutableStateOf("") }

    LaunchedEffect(roomId, currentUserId, userId) {
        Log.d("ChatScreen", "Initializing chat with roomId: $roomId")
        chatViewModel.initialize(roomId, currentUserId, userId)
    }

    LaunchedEffect(networkStatus) {
        if (networkStatus is ConnectivityStatus.Available) {
            Log.d("ChatScreen", "Re-initializing chatroom listener with roomId: $roomId")
            netActivity = ""
            chatViewModel.initializeMessageListener()
        } else
            netActivity = "Connecting..."
    }


    LaunchedEffect(chatState) {
        if (chatState is ChatState.Success) {
            chatViewModel.markMessagesAsRead()
        }
    }
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            val userData = User(
                userId = userId,
                username = username,
                profileUrl = profileUrl,
                deviceToken = deviceToken,
            )
            HeaderBar(
                userData = userData,
                name = decodedUsername,
                pic = profileUrl,
                netActivity = netActivity,
                goBack = { navController.popBackStack() },
                navController = navController,
                chatOptionsList = listOf(
                    DropMenu(
                        text = "View Profile",
                        onClick = {
                            val userJson = Uri.encode(Gson().toJson(userData))
                            navController.navigate("otherProfileScreen/$userJson")
                        },
                        icon = Icons.Default.Person
                    )
                )
            )
        },
        floatingActionButton = {
            if (showScrollToBottom) {
                ScrollToBottom {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box {
                Image(
                    painterResource(id = R.drawable.d2a77609f5d97b9081b117c8f699bd37),
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxSize(), contentScale = ContentScale.FillBounds,
                    alpha = 0.3f
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.6f)
                        .background(Color.Black)
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 0.dp, bottom = 5.dp)
                ) {
                    MessagesList(
                        messages = messages,
                        currentUserId = currentUserId,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 15.dp),
                        scrollState = listState, coroutineScope = coroutineScope,
                        roomId = roomId,
                        fontSize = fontSize.fontSize
                    )
                    MessageInput(
                        messageText = messageText,
                        onMessageChange = { messageText = it },
                        onSend = {
                            if (messageText.isNotBlank()) {
                                chatViewModel.sendMessage(
                                    content = messageText,
                                    senderName = decodedUsername
                                )
                                messageText = ""
                                vibrateDevice(context)
                            }
                        }
                    )
                }
            }
        }
    }
}

fun createRoomId(userId: String, currentUserId: String): String {
    val ids = listOf<String>(userId, currentUserId)
    return ids.sorted().joinToString("_")
}