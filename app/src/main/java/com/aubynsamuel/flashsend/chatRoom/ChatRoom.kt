package com.aubynsamuel.flashsend.chatRoom

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
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
) {
    val chatViewModel: ChatViewModel = viewModel()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: return
    val chatState by chatViewModel.chatState.collectAsState()
    val messages by chatViewModel.messages.collectAsState()

    val decodedUsername = URLDecoder.decode(username, "UTF-8")
    val decodedProfileUrl = URLDecoder.decode(profileUrl, "UTF-8")
    val context = LocalContext.current
    var messageText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val initialIndex = if (messages.isNotEmpty()) messages.size - 1 else 0
    val scrollState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    val showScrollToBottom by remember {
        derivedStateOf {
            val lastIndex = messages.size - 1
            val lastVisibleItem = scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem < lastIndex
        }
    }

    LaunchedEffect(roomId, currentUserId, userId) {
        Log.d("ChatScreen", "Initializing chat with roomId: $roomId")
        chatViewModel.initialize(roomId, currentUserId, userId)
    }

    LaunchedEffect(chatState) {
        if (chatState is ChatState.Success) {
            chatViewModel.markMessagesAsRead()
        }
    }
    Log.e("ChatRoom", "ProfileUrl: $decodedProfileUrl, roomId: $roomId")
    Log.e("ChatRoom", "Messages : $messages")
    Scaffold(
        modifier = Modifier.background(Color.White),
        topBar = {
            HeaderBar(
                name = decodedUsername,
                pic = decodedProfileUrl
            ) { navController.popBackStack() }
        },
        floatingActionButton = {
            if (showScrollToBottom) {
                ScrollToBottom {
                    coroutineScope.launch {
                        scrollState.animateScrollToItem(messages.size - 1)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 5.dp)
            ) {
                MessagesList(
                    messages = messages,
                    currentUserId = currentUserId,
                    modifier = Modifier.weight(1f),
                    scrollState = scrollState
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
                            coroutineScope.launch {
                                scrollState.animateScrollToItem(messages.size)
                            }
                        }
                    }
                )
            }
        }
    }
}
