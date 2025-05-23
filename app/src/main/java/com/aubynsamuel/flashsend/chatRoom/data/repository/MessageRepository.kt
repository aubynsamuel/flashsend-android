package com.aubynsamuel.flashsend.chatRoom.data.repository

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.aubynsamuel.flashsend.chatRoom.data.local.ChatDatabase
import com.aubynsamuel.flashsend.chatRoom.data.local.MessageDao
import com.aubynsamuel.flashsend.chatRoom.data.local.toChatMessage
import com.aubynsamuel.flashsend.chatRoom.data.local.toMessageEntity
import com.aubynsamuel.flashsend.chatRoom.data.model.MessageEntity
import com.aubynsamuel.flashsend.core.domain.logger
import com.aubynsamuel.flashsend.core.model.ChatMessage
import com.aubynsamuel.flashsend.core.model.Location
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class MessageRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val firestore: FirebaseFirestore,
) {
    private val tag = "MessageRepository"

    suspend fun createRoomIfNeeded(
        roomId: String,
        currentUserId: String,
        otherUserId: String,
    ) {
        try {
            Log.d(tag, "Checking if room exists for roomId=$roomId")
            val roomRef = firestore.collection("rooms").document(roomId)
            val room = roomRef.get().await()

            if (!room.exists()) {
                Log.d(tag, "Room does not exist. Creating new room with roomId=$roomId")
                val roomData = hashMapOf(
                    "participants" to listOf(currentUserId, otherUserId),
                    "createdAt" to Timestamp.Companion.now(),
                    "lastMessage" to "",
                    "lastMessageTimestamp" to Timestamp.Companion.now()
                )
                roomRef.set(roomData).await()
                Log.d(tag, "Room created successfully for roomId=$roomId")
            } else {
                Log.d(tag, "Room already exists for roomId=$roomId")
            }
        } catch (e: Exception) {
            logger(tag, "Error creating room if needed $e")
            throw e
        }
    }

    suspend fun getMessagesForRoom(roomId: String): List<ChatMessage> {
        return messageDao.getMessagesForRoom(roomId)
            .map { messageEntities ->
                messageEntities.map { it.toChatMessage() }
            }
            .first()
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Suppress("UNCHECKED_CAST")
    fun addMessageListener(
        roomId: String,
        onMessagesUpdated: (List<ChatMessage>) -> Unit,
        onError: (String) -> Unit,
    ): Any {
        Log.d(tag, "Initializing Firestore message listener for roomId=$roomId")
        val messagesRef = firestore.collection("rooms").document(roomId).collection("messages")
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val listener = messagesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                logger(tag, "Error in message listener: ${error.message}")
                onError("Error loading messages: ${error.message}")
                return@addSnapshotListener
            }

            snapshot?.let { querySnapshot ->
                Log.d(
                    tag,
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
                            duration = (data["duration"] as? Number)?.toLong(),
                            reactions = data["reactions"] as? MutableMap<String, String>
                                ?: mutableMapOf()
                        )
                    } catch (e: Exception) {
                        logger(
                            "chat",
                            "Error parsing message document with id=${doc.id}: ${e.message}"
                        )
                        null
                    }
                }

                onMessagesUpdated(messagesList)

                querySnapshot.documentChanges.forEach { change ->
                    if (change.type == DocumentChange.Type.REMOVED) {
                        val deletedMessageId = change.document.id
                        try {
                            GlobalScope.launch {
                                messageDao.deleteMessage(deletedMessageId)
                            }
                            Log.d(tag, "Message $deletedMessageId deleted successfully")
                        } catch (e: Exception) {
                            logger(tag, "Error deleting message: $e")
                        }
                    }
                }

                try {
                    GlobalScope.launch {
                        val messageEntities = messagesList.map { it.toMessageEntity(roomId) }
                        messageDao.insertMessages(messageEntities)
                    }
                    Log.d(tag, "Messages stored successfully")
                } catch (e: Exception) {
                    logger(tag, "Error storing messages in local database $e")
                }
            } ?: run {
                Log.d(tag, "Firestore snapshot is null")
            }
        }

        return listener
    }

    fun removeMessageListener(listener: Any) {
        if (listener is ListenerRegistration) {
            listener.remove()
            Log.d(tag, "Firestore message listener removed")
        }
    }

    suspend fun markMessagesAsRead(
        roomId: String,
        userId: String,
        messages: List<ChatMessage>,
    ) {
        try {
            val unreadMessages = messages.filter {
                !it.read && it.senderId != userId
            }

            if (unreadMessages.isNotEmpty()) {
                val batch = firestore.batch()
                unreadMessages.forEach { message ->
                    val messageRef = firestore.collection("rooms").document(roomId)
                        .collection("messages").document(message.id)
                    batch.update(messageRef, "read", true)
                }
                batch.commit().await()
                Log.d(tag, "Marking ${unreadMessages.size} messages as read")
            }
        } catch (e: Exception) {
            logger(tag, "Error marking messages as read $e")
            throw e
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun updateMessage(
        roomId: String,
        messageId: String,
        newContent: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
        context: Context,
    ) {
        val messageDao = ChatDatabase.Companion.getDatabase(context).messageDao()
        val db = FirebaseFirestore.getInstance()

        val messageRef = db.collection("rooms")
            .document(roomId)
            .collection("messages")
            .document(messageId)

        messageRef.update("content", newContent)
            .addOnSuccessListener {
                onSuccess()
                GlobalScope.launch { messageDao.editMessage(messageId, newContent) }
            }
            .addOnFailureListener { exception -> onFailure(exception) }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun prefetchNewMessagesForRoom(roomId: String) {
        val cachedMessages = messageDao.getMessagesForRoom(roomId).first()
        Log.d(tag, "Got ${cachedMessages.size} from Messages prefetch, roomId:$roomId")
        val lastCachedTime: Date = cachedMessages.firstOrNull()?.createdAt ?: Date(0)

        try {
            val querySnapshot =
                firestore.collection("rooms").document(roomId).collection("messages")
                    .whereGreaterThan("createdAt", Timestamp(lastCachedTime)).get().await()

            val newMessages = querySnapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                try {
                    MessageEntity(
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
                            val lat = (loc["latitude"] as? Number)?.toDouble() ?: 0.0
                            val lon = (loc["longitude"] as? Number)?.toDouble() ?: 0.0
                            Location(lat, lon)
                        },
                        duration = (data["duration"] as? Number)?.toLong(),
                        roomId = roomId,
                        reactions = data["reactions"] as? MutableMap<String, String>
                            ?: mutableMapOf()
                    )
                } catch (e: Exception) {
                    Log.e(tag, "Error parsing message ${doc.id}: ${e.message}")
                    null
                }
            }

            if (newMessages.isNotEmpty()) {
                messageDao.insertMessages(newMessages)
                Log.d(tag, "Inserted ${newMessages.size} new messages into local DB.")
            } else {
                Log.d(tag, "No new messages found for room: $roomId.")
            }
        } catch (e: Exception) {
            Log.e(tag, "Error fetching new messages for room $roomId: ${e.message}")
            throw e
        }
    }
}