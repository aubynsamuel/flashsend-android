package com.aubynsamuel.flashsend.chatRoom

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.aubynsamuel.flashsend.R
import com.aubynsamuel.flashsend.auth.AuthViewModel
import com.aubynsamuel.flashsend.functions.ConnectivityStatus
import com.aubynsamuel.flashsend.functions.ConnectivityViewModel
import com.aubynsamuel.flashsend.functions.NetworkConnectivityObserver
import com.aubynsamuel.flashsend.functions.User
import com.aubynsamuel.flashsend.functions.createRoomId
import com.aubynsamuel.flashsend.notifications.ConversationHistoryManager
import com.aubynsamuel.flashsend.settings.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder


@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun ChatScreen(
    navController: NavController,
    username: String,
    userId: String,
    deviceToken: String,
    profileUrl: String,
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current

//    initializations
    val auth = FirebaseAuth.getInstance()
    val chatViewModel: ChatViewModel = viewModel {
        ChatViewModel(context)
    }
    val userData by authViewModel.userData.collectAsState()
    var connectivityViewModel: ConnectivityViewModel = viewModel {
        ConnectivityViewModel(NetworkConnectivityObserver(context))
    }

//    state variables
    val currentUserId = auth.currentUser?.uid ?: return
    val roomId by remember { mutableStateOf(createRoomId(userId, currentUserId)) }
    var messageText by remember { mutableStateOf("") }
    var netActivity by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val decodedUsername = URLDecoder.decode(username, "UTF-8")
    val isRecording by chatViewModel.isRecording.collectAsState()
    val showOverlay by chatViewModel.showRecordingOverlay.collectAsState()
    val fontSize by settingsViewModel.uiState.collectAsState()
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
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val encodedUri = URLEncoder.encode(uri.toString(), "UTF-8")
            val encodedProfileUrl = URLEncoder.encode(uri.toString(), "UTF-8")
            navController.navigate(
                "imagePreview/$encodedUri/$roomId/0/${encodedProfileUrl}/$deviceToken"
            )
        }
    }
    val tempImageUri = remember {
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        // Ensure your FileProvider is configured in your manifest with the proper authority and paths
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                val encodedImage = URLEncoder.encode(tempImageUri.toString(), "UTF-8")
                val encodedProfileUrl = URLEncoder.encode(userData?.profileUrl.toString(), "UTF-8")
                navController.navigate(
                    "imagePreview/${encodedImage}/$roomId/1/${encodedProfileUrl}/$deviceToken"
                )
            } else {
                // Handle capture failure if needed
            }
        }
    )

    val permissionRequest =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
            onResult = {}
        )
    val hasCameraPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
    val hasAudioPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

//    Hooks
    LaunchedEffect(Unit) {
        if (!hasAudioPermission) {
            permissionRequest.launch(Manifest.permission.RECORD_AUDIO)
        }
        if (!hasCameraPermission) {
            permissionRequest.launch(Manifest.permission.CAMERA)
        }
    }
    LaunchedEffect(roomId, currentUserId, userId) {
        Log.d("ChatScreen", "Initializing chat with roomId: $roomId")
        chatViewModel.initialize(roomId, currentUserId, userId)
    }
    LaunchedEffect(connectivityStatus) {
        if (connectivityStatus is ConnectivityStatus.Available) {
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
                    ),
                    DropMenu(
                        text = "Settings",
                        onClick = { navController.navigate("settings") },
                        icon = Icons.Default.Settings
                    ),
                ),
                onImageClick = {
                    // Check and request camera permission
                    if (!hasCameraPermission) {
                        permissionRequest.launch(Manifest.permission.CAMERA)
                    } else {
                        // Launch camera
                        launcher.launch(tempImageUri)
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
                                    senderName = userData?.username ?: "",
                                    profileUrl = userData?.profileUrl ?: "",
                                    recipientsToken = deviceToken
                                )
                                vibrateDevice(context)
                                val newMessage = NotificationCompat.MessagingStyle.Message(
                                    messageText, System.currentTimeMillis(), "You"
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
                        onRecordAudio = { chatViewModel.toggleRecording(context) },
                        chatViewModel = chatViewModel,
                        userData = userData,
                        roomId = roomId,
                        recipientToken = deviceToken
                    )
                }
                if (showOverlay) {
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