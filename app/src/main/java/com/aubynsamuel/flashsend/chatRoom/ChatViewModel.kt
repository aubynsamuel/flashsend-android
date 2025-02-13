package com.aubynsamuel.flashsend.chatRoom

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aubynsamuel.flashsend.functions.ChatMessage
import com.aubynsamuel.flashsend.functions.Location
import com.aubynsamuel.flashsend.functions.logger
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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

    fun sendMessage(content: String, senderName: String) {
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


    override fun onCleared() {
        super.onCleared()
        Log.d("ChatViewModel", "onCleared: Removing Firestore message listener")
        messageListener?.remove()
    }
}

// Extension functions for conversions with logging.
private fun ChatMessage.toMessageEntity(roomId: String): MessageEntity {
    Log.d(
        "ChatViewModel",
        "Converting ChatMessage (id=${this.id}) to MessageEntity with roomId=$roomId"
    )
    return MessageEntity(
        id = id,
        content = content,
        image = image,
        audio = audio,
        createdAt = createdAt,
        senderId = senderId,
        senderName = senderName,
        replyTo = replyTo,
        read = read,
        type = type,
        delivered = delivered,
        location = location,
        duration = duration,
        roomId = roomId
    )
}

private fun MessageEntity.toChatMessage(): ChatMessage {
    Log.d("ChatViewModel", "Converting MessageEntity (id=${this.id}) to ChatMessage")
    return ChatMessage(
        id = id,
        content = content,
        image = image,
        audio = audio,
        createdAt = createdAt,
        senderId = senderId,
        senderName = senderName,
        replyTo = replyTo,
        read = read,
        type = type,
        delivered = delivered,
        location = location,
        duration = duration
    )
}