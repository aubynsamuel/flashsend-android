package com.aubynsamuel.flashsend.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {
    private val _rooms = MutableStateFlow<List<RoomData>>(emptyList())
    val rooms: StateFlow<List<RoomData>> = _rooms

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        auth.currentUser?.let {
            loadRooms()
        } ?: run {
            _error.value = "User not authenticated"
        }
    }

    private fun loadRooms() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val userId =
                    auth.currentUser?.uid ?: throw SecurityException("User not authenticated")
                val roomsRef = firestore.collection("rooms")

                val snapshot = roomsRef
                    .whereArrayContains("participants", userId)
                    .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val roomsList = snapshot.documents.mapNotNull { roomDoc ->
                    val data = roomDoc.data ?: return@mapNotNull null

                    @Suppress("UNCHECKED_CAST")
                    val participants =
                        data["participants"] as? List<String> ?: return@mapNotNull null
                    val otherUserId =
                        participants.firstOrNull { it != userId } ?: return@mapNotNull null

                    // Fetch other user's details from users collection
                    val userDoc = firestore.collection("users")
                        .document(otherUserId)
                        .get()
                        .await()

                    val userData = userDoc.data
                    val user = User(
                        userId = otherUserId,
                        username = userData?.get("username") as? String ?: "Unknown User",
                        profileUrl = userData?.get("profileUrl") as? String ?: "",
                        otherUsersDeviceToken = userData?.get("deviceToken") as? String ?: ""
                    )

                    RoomData(
                        roomId = roomDoc.id,
                        lastMessage = data["lastMessage"] as? String ?: "",
                        lastMessageTimestamp = data["lastMessageTimestamp"] as? Long ?: 0L,
                        lastMessageSenderId = data["lastMessageSenderId"] as? String,
                        otherParticipant = user
                    )
                }

                _rooms.value = roomsList
            } catch (e: Exception) {
                _error.value = when (e) {
                    is SecurityException -> "Authentication error: ${e.message}"
                    else -> "Failed to load rooms: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun retryLoadRooms() {
        loadRooms()
    }
}