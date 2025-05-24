package com.aubynsamuel.flashsend.home.data

import android.content.Context
import com.aubynsamuel.flashsend.core.data.MediaCacheManager
import com.aubynsamuel.flashsend.core.domain.logger
import com.aubynsamuel.flashsend.core.model.RoomData
import com.aubynsamuel.flashsend.core.model.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RoomRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val appContext: Context,
) {

    private val tag = "RoomRepository"
    private val userCache = mutableMapOf<String, User>()

    fun listenToRooms(userId: String): Flow<Result<List<RoomData>>> = callbackFlow {
        val roomsRef = firestore.collection("rooms")
        val query = roomsRef
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)

        val listenerRegistration = query.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                trySend(Result.failure(exception))
                return@addSnapshotListener
            }

            if (snapshot != null) {
                launch {
                    val roomsList = mutableListOf<RoomData>()
                    for (roomDoc in snapshot.documents) {
                        val data = roomDoc.data
                        if (data == null) continue

                        @Suppress("UNCHECKED_CAST")
                        val participants = data["participants"] as? List<String> ?: continue

                        val otherUserId = participants.firstOrNull { it != userId } ?: continue

                        val user = userCache[otherUserId] ?: try {
                            val userResult = getUser(otherUserId)
                            userResult.getOrNull()?.also { fetchedUser ->
                                userCache[otherUserId] = fetchedUser
                            }
                        } catch (e: Exception) {
                            logger(tag, "Error fetching user $otherUserId: ${e.message}")
                            null
                        }

                        if (user != null) {
                            roomsList.add(
                                RoomData(
                                    roomId = roomDoc.id,
                                    lastMessage = data["lastMessage"] as? String ?: "",
                                    lastMessageTimestamp = data["lastMessageTimestamp"] as? Timestamp,
                                    lastMessageSenderId = (data["lastMessageSenderId"] as? String).toString(),
                                    otherParticipant = user
                                )
                            )
                        }
                    }
                    trySend(Result.success(roomsList))
                }
            }
        }
        awaitClose {
            listenerRegistration.remove()
        }
    }

    suspend fun getUser(userId: String): Result<User?> {
        return try {
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            val userData = userDoc.data
            if (userData != null) {
                val originalProfileUrl = userData["profileUrl"] as? String ?: ""
                val newUser = User(
                    userId = userId,
                    username = userData["username"] as? String ?: "Unknown User",
                    profileUrl = originalProfileUrl,
                    deviceToken = userData["deviceToken"] as? String ?: ""
                )
                // Use MediaCacheManager to get a cached URI for the profile image
                val cachedUri = MediaCacheManager.getMediaUri(appContext, originalProfileUrl)
                Result.success(newUser.copy(profileUrl = cachedUri.toString()))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            logger(tag, "Error fetching user $userId: ${e.message}")
            Result.failure(e)
        }
    }
}