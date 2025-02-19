package com.aubynsamuel.flashsend.chatRoom

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aubynsamuel.flashsend.functions.ChatMessage
import com.aubynsamuel.flashsend.functions.Location
import com.aubynsamuel.flashsend.functions.logger
import com.aubynsamuel.flashsend.functions.toChatMessage
import com.aubynsamuel.flashsend.functions.toMessageEntity
import com.aubynsamuel.flashsend.notifications.NotificationRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.Date

sealed class ChatState {
    object Loading : ChatState()
    data class Success(val messages: List<ChatMessage>) : ChatState()
    data class Error(val message: String) : ChatState()
}

class ChatViewModel(context: Context) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private var messageListener: ListenerRegistration? = null
    private val storage = FirebaseStorage.getInstance()

    private val messageDao = ChatDatabase.getDatabase(context).messageDao()
    private val repository = NotificationRepository()

    private val _chatState = MutableStateFlow<ChatState>(ChatState.Loading)
    val chatState: StateFlow<ChatState> = _chatState

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private var roomId: String? = null
    private var currentUserId: String? = null
    private var otherUserId: String? = null

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var startTime: Long = 0L
    private var stopTime: Long = 0L

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    val recordingStartTime: Long
        get() = startTime


    private val _showRecordingOverlay = MutableStateFlow(false)
    val showRecordingOverlay: StateFlow<Boolean> = _showRecordingOverlay

    fun toggleRecording(context: Context) {
        if (_isRecording.value) {
            stopRecording()
            _showRecordingOverlay.value = true
        } else {
            startRecording(context)
            _showRecordingOverlay.value = true
        }
        _isRecording.value = !_isRecording.value
    }

    fun resetRecording() {
        _showRecordingOverlay.value = false
    }

    private fun startRecording(context: Context) {
        try {
            val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            audioFile =
                File.createTempFile("audio_${System.currentTimeMillis()}", ".3gp", outputDir)

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
                startTime = System.currentTimeMillis()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error starting recording", e)
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        stopTime = System.currentTimeMillis()
        mediaRecorder = null
    }

    private suspend fun uploadAudio(file: File?): String? {
        return try {
            val storageRef = storage.reference.child("chatAudio/${file?.name}")
            storageRef.putFile(Uri.fromFile(file)).await()
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Error uploading audio", e)
            null
        }
        audioFile = null
    }

    fun sendAudioMessage(senderName: String) {
        viewModelScope.launch {
            try {
                val audioUrl = uploadAudio(audioFile)
                val duration = stopTime - startTime
                roomId?.let { roomId ->
                    currentUserId?.let { userId ->
                        val messageData = hashMapOf(
                            "content" to "🔊 ${formatTime(duration)}",
                            "createdAt" to Timestamp.now(),
                            "senderId" to userId,
                            "senderName" to senderName,
                            "type" to "audio",
                            "read" to false,
                            "delivered" to false,
                            "audio" to audioUrl,
                            "duration" to duration
                        )

                        firestore.collection("rooms")
                            .document(roomId)
                            .collection("messages")
                            .add(messageData)
                            .await()

                        firestore.collection("rooms")
                            .document(roomId)
                            .update(
                                mapOf(
                                    "lastMessage" to "🔊 ${formatTime(duration)}",
                                    "lastMessageTimestamp" to Timestamp.now(),
                                    "lastMessageSenderId" to userId
                                )
                            )
                            .await()
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending audio message", e)
            }
        }
    }

    fun initialize(roomId: String, currentUserId: String, otherUserId: String) {
        this.roomId = roomId
        this.currentUserId = currentUserId
        this.otherUserId = otherUserId

        Log.d(
            "ChatViewModel",
            "Initializing chat: roomId=$roomId, currentUserId=$currentUserId, otherUserId=$otherUserId"
        )

        viewModelScope.launch {
            try {
                _chatState.value = ChatState.Loading

                // Collect messages from the local database.
                launch {
                    messageDao.getMessagesForRoom(roomId).collect { cachedMessages ->
                        Log.d(
                            "ChatViewModel",
                            "Received ${cachedMessages.size} cached messages from local database"
                        )
                        val chatMessages = cachedMessages.map { it.toChatMessage() }
                        _messages.value = chatMessages
                        _chatState.value = ChatState.Success(chatMessages)
                    }
                }

                // Check if the room exists on Firestore; create it if it doesn't.
                createRoomIfNeeded(roomId, currentUserId, otherUserId)

                // Initialize Firestore listener for real-time updates.
//                initializeMessageListener()
            } catch (e: Exception) {
                logger("chatPack", "Error initializing chat$e")
                _chatState.value = ChatState.Error("Failed to initialize chat: ${e.message}")
            }
        }
    }

    private suspend fun createRoomIfNeeded(
        roomId: String,
        currentUserId: String,
        otherUserId: String
    ) {
        try {
            Log.d("ChatViewModel", "Checking if room exists for roomId=$roomId")
            val roomRef = firestore.collection("rooms").document(roomId)
            val room = roomRef.get().await()

            if (!room.exists()) {
                Log.d("ChatViewModel", "Room does not exist. Creating new room with roomId=$roomId")
                val roomData = hashMapOf(
                    "participants" to listOf(currentUserId, otherUserId),
                    "createdAt" to Timestamp.now(),
                    "lastMessage" to "",
                    "lastMessageTimestamp" to Timestamp.now()
                )
                roomRef.set(roomData).await()
                Log.d("ChatViewModel", "Room created successfully for roomId=$roomId")
            } else {
                Log.d("ChatViewModel", "Room already exists for roomId=$roomId")
            }
        } catch (e: Exception) {
            logger("chatPack", "Error creating room if needed $e")
            throw e
        }
    }

    fun initializeMessageListener() {
        roomId?.let { roomId ->
            messageListener?.remove()
            Log.d("ChatViewModel", "Initializing Firestore message listener for roomId=$roomId")
            val messagesRef = firestore.collection("rooms")
                .document(roomId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.DESCENDING)

            messageListener = messagesRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logger("chatPack", "Error in message listener: ${error.message}")
                    _chatState.value = ChatState.Error("Error loading messages: ${error.message}")
                    return@addSnapshotListener
                }

                snapshot?.let { querySnapshot ->
                    Log.d(
                        "ChatViewModel",
                        "Firestore snapshot received with ${querySnapshot.documents.size} documents"
                    )
                    val messagesList = querySnapshot.documents.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: return@mapNotNull null
                            ChatMessage(
                                id = doc.id,
                                content = data["content"] as? String ?: "",
                                image = data["image"] as? String,
                                audio = data["audio"] as? String,
                                createdAt = (data["createdAt"] as? Timestamp)?.toDate() ?: Date(),
                                senderId = data["senderId"] as? String ?: "",
                                senderName = data["senderName"] as? String ?: "",
                                replyTo = data["replyTo"] as? String,
                                read = data["read"] as? Boolean == true,
                                type = data["type"] as? String ?: "text",
                                delivered = data["delivered"] as? Boolean == true,
                                location = (data["location"] as? Map<*, *>)?.let { loc ->
                                    Location(
                                        latitude = (loc["latitude"] as? Number)?.toDouble() ?: 0.0,
                                        longitude = (loc["longitude"] as? Number)?.toDouble() ?: 0.0
                                    )
                                },
                                duration = (data["duration"] as? Number)?.toLong()
                            )
                        } catch (e: Exception) {
                            logger(
                                "chat",
                                "Error parsing message document with id=${doc.id}: ${e.message}",

                                )
                            null
                        }
                    }
                    Log.d("ChatViewModel", "Processed ${messagesList.size} messages from snapshot")
                    _messages.value = messagesList
                    viewModelScope.launch {
                        val messageEntities = messagesList.map { it.toMessageEntity(roomId) }
                        try {
                            messageDao.insertMessages(messageEntities)
                            Log.d("ChatViewModel", "Messages stored successfully")
                        } catch (e: Exception) {
                            logger("chatPack", "Error storing messages in local database $e")
                        }
                    }
                    _chatState.value = ChatState.Success(messagesList)
                } ?: run {
                    Log.d("ChatViewModel", "Firestore snapshot is null")
                }
            }
        } ?: run {
            logger("chatPack", "RoomId is null when trying to initialize message listener")
            _chatState.value = ChatState.Error("Room ID is not set")
        }
    }

    fun markMessagesAsRead() {
        viewModelScope.launch {
            try {
                roomId?.let { roomId ->
                    currentUserId?.let { userId ->
                        val unreadMessages = messages.value.filter {
                            !it.read && it.senderId != userId
                        }
                        Log.d("ChatViewModel", "Marking ${unreadMessages.size} messages as read")
                        unreadMessages.forEach { message ->
                            Log.d("ChatViewModel", "Marking message id=${message.id} as read")
                            firestore.collection("rooms")
                                .document(roomId)
                                .collection("messages")
                                .document(message.id)
                                .update("read", true)
                                .await()
                        }
                    }
                }
            } catch (e: Exception) {
                logger("chatPack", "Error marking messages as read $e")
            }
        }
    }

    fun onSendNotification(
        recipientsToken: String,
        title: String,
        body: String,
        roomId: String,
        recipientsUserId: String,
        sendersUserId: String,
        profileUrl: String
    ) {
        viewModelScope.launch {
            try {
                repository.sendNotification(
                    recipientsToken = recipientsToken,
                    title = title,
                    body = body,
                    roomId = roomId,
                    recipientsUserId = recipientsUserId,
                    sendersUserId = sendersUserId,
                    profileUrl = profileUrl
                )
            } catch (e: Exception) {
                logger("NetWorkError", e.message.toString())
            }
        }
    }

    fun sendMessage(
        content: String,
        senderName: String,
        recipientsToken: String,
        profileUrl: String
    ) {
        viewModelScope.launch {
            try {
                roomId?.let { roomId ->
                    currentUserId?.let { userId ->
                        Log.d(
                            "ChatViewModel",
                            "Sending message: content='$content', senderId=$userId, senderName=$senderName, roomId=$roomId"
                        )
                        val messageData = hashMapOf(
                            "content" to content,
                            "createdAt" to Timestamp.now(),
                            "senderId" to userId,
                            "senderName" to senderName,
                            "type" to "text",
                            "read" to false,
                            "delivered" to false
                        )

                        // Add message to Firestore.
                        val addedDoc = firestore.collection("rooms")
                            .document(roomId)
                            .collection("messages")
                            .add(messageData)
                            .await()
                        Log.d(
                            "ChatViewModel",
                            "Message sent to Firestore with document id=${addedDoc.id}"
                        )

                        // Update room's last message.
                        firestore.collection("rooms")
                            .document(roomId)
                            .update(
                                mapOf(
                                    "lastMessage" to content,
                                    "lastMessageTimestamp" to Timestamp.now(),
                                    "lastMessageSenderId" to userId
                                )
                            )
                            .await()
                        // send notification
                        onSendNotification(
                            recipientsToken, senderName, content, roomId,
                            otherUserId.toString(), userId, profileUrl
                        )
                        Log.d(
                            "ChatViewModel",
                            "Room's last message updated successfully for roomId=$roomId"
                        )
                    }
                }
            } catch (e: Exception) {
                logger("chatPack", "Error sending message $e")
                _chatState.value = ChatState.Error("Failed to send message: ${e.message}")
            }
        }
    }

    // New function to send image messages.
    fun sendImageMessage(
        caption: String,
        imageUrl: String,
        senderName: String,
        roomId: String,
        currentUserId: String,
    ) {
        logger(
            "sendImage",
            "Caption: $caption," +
                    "ImageUrl: $imageUrl," +
                    "SenderName: $senderName," +
                    "RoomId: $roomId," +
                    "CurrentUserId: $currentUserId"
        )
        viewModelScope.launch {
            try {
                roomId.let { roomId ->
                    currentUserId.let { userId ->
                        val messageData = hashMapOf(
                            "content" to caption,
                            "createdAt" to Timestamp.now(),
                            "senderId" to userId,
                            "senderName" to senderName,
                            "type" to "image",
                            "read" to false,
                            "delivered" to false,
                            "image" to imageUrl
                        )
                        val addedDoc = firestore.collection("rooms")
                            .document(roomId)
                            .collection("messages")
                            .add(messageData)
                            .await()
                        Log.d("ChatViewModel", "Image message sent with id=${addedDoc.id}")

                        firestore.collection("rooms")
                            .document(roomId)
                            .update(
                                mapOf(
                                    "lastMessage" to "📷 Sent an image",
                                    "lastMessageTimestamp" to Timestamp.now(),
                                    "lastMessageSenderId" to userId
                                )
                            )
                            .await()
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending image message", e)
            }
        }
    }

    // Function to upload an image to Firebase Storage and return the download URL.
    suspend fun uploadImage(imageUri: Uri, username: String): String? {
        return try {
            // Use a unique filename (you could also use the image’s original name)
            val storageRef =
                storage.reference.child("chatMedia/${username}_${System.currentTimeMillis()}.jpg")
            storageRef.putFile(imageUri).await()
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Error uploading image", e)
            null
        }
    }

    fun sendLocationMessage(
        latitude: Double,
        longitude: Double,
        senderName: String,
        roomId: String,
        currentUserId: String
    ) {
        viewModelScope.launch {
            try {
                // Create a map for the location data.
                val locationData = mapOf(
                    "latitude" to latitude,
                    "longitude" to longitude
                )
                val messageData = hashMapOf(
                    "content" to "$locationData",
                    "createdAt" to Timestamp.now(),
                    "senderId" to currentUserId,
                    "senderName" to senderName,
                    "type" to "location",
                    "location" to locationData,
                    "read" to false,
                    "delivered" to false
                )

                // Add the message document to Firestore.
                val addedDoc = firestore.collection("rooms")
                    .document(roomId)
                    .collection("messages")
                    .add(messageData)
                    .await()
                Log.d("ChatViewModel", "Location message sent with id=${addedDoc.id}")

                // Update the room's last message.
                firestore.collection("rooms")
                    .document(roomId)
                    .update(
                        mapOf(
                            "lastMessage" to "Shared a location",
                            "lastMessageTimestamp" to Timestamp.now(),
                            "lastMessageSenderId" to currentUserId
                        )
                    )
                    .await()
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending location message", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("ChatViewModel", "onCleared: Removing Firestore message listener")
        messageListener?.remove()
    }
}