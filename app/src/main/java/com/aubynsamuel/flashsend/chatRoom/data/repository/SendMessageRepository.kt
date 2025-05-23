package com.aubynsamuel.flashsend.chatRoom.data.repository

import android.util.Log
import com.aubynsamuel.flashsend.core.domain.logger
import com.aubynsamuel.flashsend.core.model.ChatMessage
import com.aubynsamuel.flashsend.core.model.Location
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SendMessageRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val tag = "MessageRepository"

    suspend fun sendTextMessage(
        roomId: String,
        content: String,
        senderId: String,
        senderName: String,
    ) {
        try {
            Log.d(
                tag,
                "Sending message: content='$content', senderId=$senderId, senderName=$senderName, roomId=$roomId"
            )
            val messageData = hashMapOf(
                "content" to content,
                "createdAt" to Timestamp.now(),
                "senderId" to senderId,
                "senderName" to senderName,
                "type" to "text",
                "read" to false,
                "delivered" to false
            )

            // Add message to Firestore
            val addedDoc = firestore.collection("rooms").document(roomId).collection("messages")
                .add(messageData).await()
            Log.d(tag, "Message sent to Firestore with document id=${addedDoc.id}")

            // Update room's last message
            updateRoomLastMessage(roomId, content, senderId)
        } catch (e: Exception) {
            logger(tag, "Error sending message $e")
            throw e
        }
    }

    suspend fun sendAudioMessage(
        roomId: String,
        content: String,
        senderId: String,
        senderName: String,
        audioUrl: String?,
        duration: Long,
    ) {
        try {
            val messageData = hashMapOf(
                "content" to content,
                "createdAt" to Timestamp.now(),
                "senderId" to senderId,
                "senderName" to senderName,
                "type" to "audio",
                "read" to false,
                "delivered" to false,
                "audio" to audioUrl,
                "duration" to duration
            )

            firestore.collection("rooms").document(roomId).collection("messages")
                .add(messageData).await()

            updateRoomLastMessage(roomId, content, senderId)
        } catch (e: Exception) {
            Log.e(tag, "Error sending audio message", e)
            throw e
        }
    }

    suspend fun sendImageMessage(
        roomId: String,
        caption: String,
        imageUrl: String,
        senderId: String,
        senderName: String,
    ) {
        try {
            val messageData = hashMapOf(
                "content" to caption,
                "createdAt" to Timestamp.now(),
                "senderId" to senderId,
                "senderName" to senderName,
                "type" to "image",
                "read" to false,
                "delivered" to false,
                "image" to imageUrl
            )

            val addedDoc = firestore.collection("rooms").document(roomId).collection("messages")
                .add(messageData).await()
            Log.d(tag, "Image message sent with id=${addedDoc.id}")

            updateRoomLastMessage(roomId, "📷 Sent an image", senderId)
        } catch (e: Exception) {
            Log.e(tag, "Error sending image message", e)
            throw e
        }
    }

    suspend fun sendLocationMessage(
        roomId: String,
        senderId: String,
        senderName: String,
        location: Location,
    ) {
        try {
            // Create a map for the location data
            val locationData = mapOf(
                "latitude" to location.latitude,
                "longitude" to location.longitude
            )

            val messageData = hashMapOf(
                "content" to "$locationData",
                "createdAt" to Timestamp.now(),
                "senderId" to senderId,
                "senderName" to senderName,
                "type" to "location",
                "location" to locationData,
                "read" to false,
                "delivered" to false
            )

            val addedDoc = firestore.collection("rooms").document(roomId).collection("messages")
                .add(messageData).await()
            Log.d(tag, "Location message sent with id=${addedDoc.id}")

            updateRoomLastMessage(roomId, "Shared a location", senderId)
        } catch (e: Exception) {
            Log.e(tag, "Error sending location message", e)
            throw e
        }
    }

    fun addReactionToMessage(
        roomId: String,
        messageId: String,
        userId: String,
        emoji: String,
        messageContent: String,
    ) {
        try {
            Log.e(tag, "Adding reaction")
            val messageRef = firestore.collection("rooms").document(roomId).collection("messages")
                .document(messageId)

            messageRef.get().addOnSuccessListener { document ->
                val message = document.toObject(ChatMessage::class.java)
                message?.let {
                    val updatedReactions = it.reactions.toMutableMap()

                    if (updatedReactions[userId] == emoji) {
                        updatedReactions.remove(userId) // Remove reaction if already present
                    } else {
                        updatedReactions[userId] = emoji // Add or update reaction
                    }

                    messageRef.update("reactions", updatedReactions).addOnSuccessListener {
                        Log.e(tag, "Reaction Added Successfully")
                        // Now update the room's last message
                        firestore.collection("rooms").document(roomId).update(
                            mapOf(
                                "lastMessage" to "reacted $emoji to $messageContent",
                                "lastMessageTimestamp" to Timestamp.now(),
                                "lastMessageSenderId" to userId
                            )
                        ).addOnSuccessListener {
                            Log.e(tag, "Room's last message updated for reaction")
                        }.addOnFailureListener { e ->
                            Log.e(
                                tag,
                                "Failed to update room's last message: ${e.message}"
                            )
                        }
                    }.addOnFailureListener { e ->
                        Log.e(tag, "Failed to update reactions: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error updating reaction", e)
            throw e
        }
    }

    suspend fun updateRoomLastMessage(
        roomId: String,
        lastMessage: String,
        senderId: String,
    ) {
        try {
            firestore.collection("rooms").document(roomId).update(
                mapOf(
                    "lastMessage" to lastMessage,
                    "lastMessageTimestamp" to Timestamp.now(),
                    "lastMessageSenderId" to senderId
                )
            ).await()
            Log.d(tag, "Room's last message updated successfully for roomId=$roomId")
        } catch (e: Exception) {
            logger(tag, "Error updating room's last message: $e")
            throw e
        }
    }
}