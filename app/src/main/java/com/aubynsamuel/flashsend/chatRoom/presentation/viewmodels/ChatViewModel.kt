package com.aubynsamuel.flashsend.chatRoom.presentation.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aubynsamuel.flashsend.chatRoom.domain.AddMessageListenerUseCase
import com.aubynsamuel.flashsend.chatRoom.domain.AddReactionUseCase
import com.aubynsamuel.flashsend.chatRoom.domain.AudioRecordingUseCase
import com.aubynsamuel.flashsend.chatRoom.domain.GetMessagesUseCase
import com.aubynsamuel.flashsend.chatRoom.domain.InitializeChatUseCase
import com.aubynsamuel.flashsend.chatRoom.domain.MarkMessagesAsReadUseCase
import com.aubynsamuel.flashsend.chatRoom.domain.PrefetchMessagesUseCase
import com.aubynsamuel.flashsend.chatRoom.domain.RemoveMessageListenerUseCase
import com.aubynsamuel.flashsend.chatRoom.domain.SendImageMessageUseCase
import com.aubynsamuel.flashsend.chatRoom.domain.SendLocationMessageUseCase
import com.aubynsamuel.flashsend.chatRoom.domain.SendTextMessageUseCase
import com.aubynsamuel.flashsend.chatRoom.domain.UpdateMessageUseCase
import com.aubynsamuel.flashsend.chatRoom.domain.UploadImageUseCase
import com.aubynsamuel.flashsend.core.domain.logger
import com.aubynsamuel.flashsend.core.model.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ChatState {
    object Loading : ChatState()
    data class Success(val messages: List<ChatMessage>) : ChatState()
    data class Error(val message: String) : ChatState()
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getMessagesUseCase: GetMessagesUseCase,
    private val initializeChatUseCase: InitializeChatUseCase,
    private val addMessageListenerUseCase: AddMessageListenerUseCase,
    private val removeMessageListenerUseCase: RemoveMessageListenerUseCase,
    private val markMessagesAsReadUseCase: MarkMessagesAsReadUseCase,
    private val sendTextMessageUseCase: SendTextMessageUseCase,
    private val sendImageMessageUseCase: SendImageMessageUseCase,
    private val sendLocationMessageUseCase: SendLocationMessageUseCase,
    private val uploadImageUseCase: UploadImageUseCase,
    private val addReactionUseCase: AddReactionUseCase,
    private val prefetchMessagesUseCase: PrefetchMessagesUseCase,
    private val audioRecordingUseCase: AudioRecordingUseCase,
    private val updateMessageUseCase: UpdateMessageUseCase,
) : ViewModel() {
    private val tag = "ChatViewModel"
    val unreadRoomIds = mutableStateListOf<String>()
    private var messageListener: Any? = null
    private val _chatState = MutableStateFlow<ChatState>(ChatState.Loading)
    val chatState: StateFlow<ChatState> = _chatState
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages
    private var roomId: String? = null
    private var currentUserId: String? = null
    private var otherUserId: String? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording
    val recordingStartTime: Long
        get() = audioRecordingUseCase.recordingStartTime

    private val _showRecordingOverlay = MutableStateFlow(false)
    val showRecordingOverlay: StateFlow<Boolean> = _showRecordingOverlay

    fun initialize(roomId: String, currentUserId: String, otherUserId: String) {
        this.roomId = roomId
        this.currentUserId = currentUserId
        this.otherUserId = otherUserId

        Log.d(
            tag,
            "Initializing chat: roomId=$roomId, currentUserId=$currentUserId, otherUserId=$otherUserId"
        )

        viewModelScope.launch {
            try {
                _chatState.value = ChatState.Loading

                // Create the room if needed
                initializeChatUseCase(roomId, currentUserId, otherUserId)

                // Subscribe to messages from the local database
                var retrievedMessages: List<ChatMessage> = getMessagesUseCase(roomId)
                Log.d(tag, "Received ${retrievedMessages.size} messages")
                _messages.value = retrievedMessages
                _chatState.value = ChatState.Success(retrievedMessages)
            } catch (e: Exception) {
                logger(tag, "Error initializing chat: $e")
                _chatState.value = ChatState.Error("Failed to initialize chat: ${e.message}")
            }
        }
    }

    fun initializeMessageListener() {
        roomId?.let { roomId ->
            viewModelScope.launch {
                messageListener?.let {
                    removeMessageListenerUseCase(it)
                }

                messageListener = addMessageListenerUseCase(
                    roomId = roomId,
                    onMessagesUpdated = { messages ->
                        _messages.value = messages
                        _chatState.value = ChatState.Success(messages)
                    },
                    onError = { errorMessage ->
                        _chatState.value = ChatState.Error(errorMessage)
                    }
                )
            }
        } ?: run {
            logger(tag, "RoomId is null when trying to initialize message listener")
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
                        if (unreadMessages.isNotEmpty()) {
                            markMessagesAsReadUseCase(roomId, userId, unreadMessages)
                        }
                    }
                }
            } catch (e: Exception) {
                logger(tag, "Error marking messages as read: $e")
            }
        }
    }

    fun toggleRecording(context: Context) {
        if (_isRecording.value) {
            audioRecordingUseCase.stopRecording()
            _showRecordingOverlay.value = true
        } else {
            audioRecordingUseCase.startRecording(context)
            _showRecordingOverlay.value = true
        }
        _isRecording.value = !_isRecording.value
    }

    fun resetRecording() {
        _showRecordingOverlay.value = false
    }

    fun sendMessage(
        content: String, senderName: String, recipientsToken: String, profileUrl: String,
    ) {
        viewModelScope.launch {
            try {
                roomId?.let { roomId ->
                    currentUserId?.let { userId ->
                        sendTextMessageUseCase(
                            roomId = roomId,
                            content = content,
                            senderId = userId,
                            senderName = senderName,
                            recipientsToken = recipientsToken,
                            otherUserId = otherUserId ?: "",
                            profileUrl = profileUrl
                        )
                    }
                }
            } catch (e: Exception) {
                logger(tag, "Error sending message: $e")
                _chatState.value = ChatState.Error("Failed to send message: ${e.message}")
            }
        }
    }

    fun sendAudioMessage(senderName: String, profileUrl: String, recipientsToken: String) {
        viewModelScope.launch {
            roomId?.let { roomId ->
                currentUserId?.let { userId ->
                    try {
                        audioRecordingUseCase.sendAudioMessage(
                            roomId = roomId,
                            senderId = userId,
                            senderName = senderName,
                            otherUserId = otherUserId ?: "",
                            profileUrl = profileUrl,
                            recipientsToken = recipientsToken
                        )
                    } catch (e: Exception) {
                        logger(tag, "Error sending audio message: $e")
                    }
                }
            }
        }
    }

    fun sendImageMessage(
        caption: String,
        imageUrl: String,
        senderName: String,
        roomId: String,
        currentUserId: String,
        profileUrl: String,
        recipientsToken: String,
    ) {
        viewModelScope.launch {
            try {
                sendImageMessageUseCase(
                    caption = caption,
                    imageUrl = imageUrl,
                    senderName = senderName,
                    roomId = roomId,
                    senderId = currentUserId,
                    otherUserId = otherUserId ?: "",
                    profileUrl = profileUrl,
                    recipientsToken = recipientsToken
                )
            } catch (e: Exception) {
                logger(tag, "Error sending image message: $e")
            }
        }
    }

    fun sendLocationMessage(
        latitude: Double,
        longitude: Double,
        senderName: String,
        roomId: String,
        currentUserId: String,
        profileUrl: String,
        recipientsToken: String,
    ) {
        viewModelScope.launch {
            try {
                sendLocationMessageUseCase(
                    latitude = latitude,
                    longitude = longitude,
                    senderName = senderName,
                    roomId = roomId,
                    senderId = currentUserId,
                    otherUserId = otherUserId ?: "",
                    profileUrl = profileUrl,
                    recipientsToken = recipientsToken
                )
            } catch (e: Exception) {
                logger(tag, "Error sending location message: $e")
            }
        }
    }

    suspend fun uploadImage(imageUri: Uri, username: String): String? {
        return uploadImageUseCase(imageUri, username)
    }

    fun addReactionToMessage(
        roomId: String,
        messageId: String,
        userId: String,
        emoji: String,
        messageContent: String,
    ) {
        viewModelScope.launch {
            try {
                addReactionUseCase(
                    roomId = roomId,
                    messageId = messageId,
                    userId = userId,
                    emoji = emoji,
                    messageContent = messageContent,
                )
            } catch (e: Exception) {
                logger(tag, "Error adding reaction: $e")
            }
        }
    }

    fun UpdateMessage(
        roomId: String,
        messageId: String,
        newContent: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        updateMessageUseCase(
            roomId,
            messageId,
            newContent,
            onSuccess,
            onFailure,
        )
    }

    suspend fun prefetchNewMessagesForRoom(roomId: String) {
        try {
            prefetchMessagesUseCase(roomId)
        } catch (e: Exception) {
            logger(tag, "Error prefetching messages: $e")
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(tag, "onCleared: Removing Firestore message listener and media recorder")
        viewModelScope.launch {
            messageListener?.let {
                removeMessageListenerUseCase(it)
            }
        }
        audioRecordingUseCase.reset()
    }
}