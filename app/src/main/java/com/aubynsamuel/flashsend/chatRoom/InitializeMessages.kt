package com.aubynsamuel.flashsend.chatRoom

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

data class ChatMessage(
    val id: String = "",
    val content: String = "",
    val image: String? = null,
    val audio: String? = null,
    val createdAt: Date = Date(),
    val senderId: String = "",
    val senderName: String = "",
    val replyTo: String? = null,
    val read: Boolean = false,
    val type: String = "text",
    val delivered: Boolean = false,
    val location: Location? = null,
    val duration: Long? = null
)

data class Location(
    val latitude: Double,
    val longitude: Double
)

sealed class ChatState {
    object Loading : ChatState()
    data class Success(val messages: List<ChatMessage>) : ChatState()
    data class Error(val message: String) : ChatState()
}

class ChatViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private var messageListener: ListenerRegistration? = null

    private val _chatState = MutableStateFlow<ChatState>(ChatState.Loading)
    val chatState: StateFlow<ChatState> = _chatState

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private var roomId: String? = null
    private var currentUserId: String? = null
    private var otherUserId: String? = null

    fun initialize(roomId: String, currentUserId: String, otherUserId: String) {
        this.roomId = roomId
        this.currentUserId = currentUserId
        this.otherUserId = otherUserId

        viewModelScope.launch {
            try {
                _chatState.value = ChatState.Loading
                // First create/verify the room exists
                createRoomIfNeeded(roomId, currentUserId, otherUserId)
                // Then initialize the message listener
                initializeMessageListener()
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error initializing chat", e)
                _chatState.value = ChatState.Error("Failed to initialize chat: ${e.message}")
            }
        }
    }

    private suspend fun createRoomIfNeeded(
        roomId: String,
        currentUserId: String,
        otherUserId: String
    ) {
        val roomRef = firestore.collection("rooms").document(roomId)
        val room = roomRef.get().await()

        if (!room.exists()) {
            val roomData = hashMapOf(
                "participants" to listOf(currentUserId, otherUserId),
                "createdAt" to Timestamp.now(),
                "lastMessage" to "",
                "lastMessageTimestamp" to Timestamp.now()
            )
            roomRef.set(roomData).await()
        }
    }

    private fun initializeMessageListener() {
        roomId?.let { roomId ->
            messageListener?.remove()

            val messagesRef = firestore.collection("rooms")
                .document(roomId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)  // Changed to ASCENDING

            messageListener = messagesRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatViewModel", "Error in message listener: ${error.message}")
                    _chatState.value = ChatState.Error("Error loading messages: ${error.message}")
                    return@addSnapshotListener
                }

                snapshot?.let { querySnapshot ->
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
                            Log.e("ChatViewModel", "Error parsing message: ${e.message}")
                            null
                        }
                    }
                    Log.d("ChatViewModel", "Loaded ${messagesList.size} messages")
                    _messages.value = messagesList
                    _chatState.value = ChatState.Success(messagesList)
                }
            }
        } ?: run {
            Log.e("ChatViewModel", "RoomId is null")
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

                        unreadMessages.forEach { message ->
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
                Log.e("ChatViewModel", "Error marking messages as read", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        messageListener?.remove()
    }

    fun sendMessage(content: String, senderName: String) {
        viewModelScope.launch {
            try {
                roomId?.let { roomId ->
                    currentUserId?.let { userId ->
                        val messageData = hashMapOf(
                            "content" to content,
                            "createdAt" to Timestamp.now(),
                            "senderId" to userId,
                            "senderName" to senderName,
                            "type" to "text",
                            "read" to false,
                            "delivered" to false
                        )

                        // Add message to Firestore
                        firestore.collection("rooms")
                            .document(roomId)
                            .collection("messages")
                            .add(messageData)
                            .await()

                        // Update room's last message
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
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message", e)
                _chatState.value = ChatState.Error("Failed to send message: ${e.message}")
            }
        }
    }
}