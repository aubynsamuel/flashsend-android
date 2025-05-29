package com.aubynsamuel.flashsend.chatRoom.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.aubynsamuel.flashsend.R
import com.aubynsamuel.flashsend.chatRoom.presentation.components.AudioRecordingOverlay
import com.aubynsamuel.flashsend.chatRoom.presentation.components.DropMenu
import com.aubynsamuel.flashsend.chatRoom.presentation.components.EmptyChatPlaceholder
import com.aubynsamuel.flashsend.chatRoom.presentation.components.HeaderBar
import com.aubynsamuel.flashsend.chatRoom.presentation.components.MessageInput
import com.aubynsamuel.flashsend.chatRoom.presentation.components.MessagesList
import com.aubynsamuel.flashsend.chatRoom.presentation.components.ScrollToBottom
import com.aubynsamuel.flashsend.chatRoom.presentation.utils.vibrateDevice
import com.aubynsamuel.flashsend.chatRoom.presentation.viewmodels.ChatState
import com.aubynsamuel.flashsend.chatRoom.presentation.viewmodels.ChatViewModel
import com.aubynsamuel.flashsend.core.data.ConnectivityStatus
import com.aubynsamuel.flashsend.core.domain.createRoomId
import com.aubynsamuel.flashsend.core.model.User
import com.aubynsamuel.flashsend.core.presentation.ConnectivityViewModel
import com.aubynsamuel.flashsend.core.state.CurrentUser
import com.aubynsamuel.flashsend.navigation.Screen
import com.aubynsamuel.flashsend.notifications.data.services.ConversationHistoryManager
import com.aubynsamuel.flashsend.notifications.data.services.person
import com.aubynsamuel.flashsend.settings.presentation.viewmodels.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.delay
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
    settingsViewModel: SettingsViewModel,
) {
    val tag = "ChatRoom"
    val context = LocalContext.current

//    initializations
    val auth = FirebaseAuth.getInstance()
    val chatViewModel: ChatViewModel = hiltViewModel()
    val userData by CurrentUser.userData.collectAsStateWithLifecycle()

    var connectivityViewModel: ConnectivityViewModel = hiltViewModel()

//    state variables
    val currentUserId = auth.currentUser?.uid ?: return
    val roomId by remember { mutableStateOf(createRoomId(userId, currentUserId)) }
    var messageText by remember { mutableStateOf("") }
    var netActivity by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val decodedUsername = URLDecoder.decode(username, "UTF-8")
    val isRecording by chatViewModel.isRecording.collectAsState()
    val showOverlay by chatViewModel.showRecordingOverlay.collectAsState()
    val fontSize by settingsViewModel.settingsState.collectAsState()
    val chatState by chatViewModel.chatState.collectAsState()
    val messages by chatViewModel.messages.collectAsState()
    val connectivityStatus by connectivityViewModel.connectivityStatus.collectAsStateWithLifecycle()

//     functions
//    val messages = generateMockMessages(currentUserId)
    val showScrollToBottom by remember {
        derivedStateOf {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            firstVisibleIndex - 1 > 0
        }
    }

    val audioPermission = Manifest.permission.RECORD_AUDIO

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val route = Screen.ImagePreview.createRoute(
                imageUri = it.toString(),
                roomId = roomId,
                takenFromCamera = false,
                profileUrl = userData?.profileUrl.orEmpty(),
                recipientsToken = deviceToken
            )
            navController.navigate(route) {
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            chatViewModel.toggleRecording(context)
        } else {
            Toast.makeText(context, "Audio recording permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val route = Screen.CameraX.createRoute(
                roomId = roomId,
                profileUrl = userData?.profileUrl ?: "",
                deviceToken = deviceToken
            )
            navController.navigate(route) {
                launchSingleTop = true
                restoreState = true
            }
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(roomId, currentUserId, userId) {
        Log.d(tag, "Initializing chat with roomId: $roomId")
        chatViewModel.initialize(roomId, currentUserId, userId)
    }
    LaunchedEffect(connectivityStatus) {
        if (connectivityStatus is ConnectivityStatus.Available) {
            Log.d(tag, "Re-initializing chatroom listener with roomId: $roomId")
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
    var previousMessageCount by rememberSaveable { mutableIntStateOf(messages.size) }

    LaunchedEffect(messages.size) {
        if (messages.size > previousMessageCount) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(0)
            }
        }
        previousMessageCount = messages.size
    }

    var showEmptyMessagesAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(500)
        showEmptyMessagesAnimation = true
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
                            navController.navigate("otherProfileScreen/$userJson") {
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = Icons.Default.Person
                    )
                ),
                onImageClick = {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val route = Screen.CameraX.createRoute(
                            roomId = roomId,
                            profileUrl = userData.profileUrl,
                            deviceToken = deviceToken
                        )
                        navController.navigate(route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
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
//                Background Image
                Image(
                    painterResource(id = R.drawable.chat_room_background),
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxSize(), contentScale = ContentScale.FillBounds,
                    alpha = 0.3f
                )
//                Background Image filter
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.6f)
                        .background(Color.Black)
                )
//                Chat list and input field
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 0.dp, bottom = 5.dp)
                ) {
                    if (messages.isEmpty() && showEmptyMessagesAnimation) {
                        EmptyChatPlaceholder(
                            lottieAnimation = R.raw.chat,
                            message = "Send a message to start a conversation",
                            speed = 1f,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 15.dp),
                            color = Color.White
                        )
                    } else {
                        MessagesList(
                            messages = messages,
                            currentUserId = currentUserId,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 15.dp),
                            scrollState = listState,
                            roomId = roomId,
                            fontSize = fontSize.fontSize,
                            chatViewModel = chatViewModel
                        )
                    }
                    MessageInput(
                        messageText = messageText,
                        onMessageChange = { messageText = it },
                        onSend = {
                            if (messageText.isNotBlank()) {
                                chatViewModel.sendMessage(
                                    content = messageText,
                                    senderName = userData?.username ?: "",
                                    profileUrl = userData?.profileUrl ?: "",
                                    recipientsToken = deviceToken
                                )
                                vibrateDevice(context)
                                val newMessage = NotificationCompat.MessagingStyle.Message(
                                    messageText, System.currentTimeMillis(), person
                                )
                                val hasMessages = ConversationHistoryManager.hasMessages(roomId)
                                if (hasMessages) {
                                    ConversationHistoryManager.addMessage(roomId, newMessage)
                                }
                                messageText = ""
                            }
                        },
                        onImageClick = { imagePickerLauncher.launch("image/*") },
                        isRecording = isRecording,
                        onRecordAudio = {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    audioPermission
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                chatViewModel.toggleRecording(context)
                            } else {
                                audioPermissionLauncher.launch(audioPermission)
                            }
                        },
                        chatViewModel = chatViewModel,
                        userData = userData,
                        roomId = roomId,
                        recipientToken = deviceToken
                    )
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = showOverlay,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 70.dp)
                ) {
                    AudioRecordingOverlay(
                        isRecording = isRecording,
                        resetRecording = { chatViewModel.resetRecording() },
                        sendAudioMessage = {
                            chatViewModel.sendAudioMessage(
                                senderName = userData?.username ?: "",
                                profileUrl = userData?.profileUrl ?: "",
                                recipientsToken = deviceToken
                            )
                        },
                        recordingStartTime = chatViewModel.recordingStartTime,
                    )
                }
            }
        }
    }
}