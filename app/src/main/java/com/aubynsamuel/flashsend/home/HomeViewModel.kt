package com.aubynsamuel.flashsend.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aubynsamuel.flashsend.functions.RoomData
import com.aubynsamuel.flashsend.functions.User
import com.aubynsamuel.flashsend.functions.logger
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel(context: Context) : ViewModel() {
    private val _rooms = MutableStateFlow<List<RoomData>>(emptyList())
    val rooms: StateFlow<List<RoomData>> = _rooms

    private val cacheHelper = CacheHelper(context = context)


    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Cache to store user data. Since this data changes infrequently, we fetch it only once.
    private val userCache = mutableMapOf<String, User>()

    // Keep a reference to the Firestore listener so we can remove it when needed.
    private var roomsListenerRegistration: ListenerRegistration? = null

    private fun loadCachedRooms() {
        viewModelScope.launch {
            val cachedRooms = cacheHelper.loadRooms()
            _rooms.value = cachedRooms
            logger("RoomCache", "Cached rooms loaded ${cachedRooms.size}")
        }
    }

    init {
        if (auth.currentUser != null) {
            loadCachedRooms()
            listenToRooms()
        } else {
            _error.value = "User not authenticated"
        }
    }

    /**
     * Listen to changes in rooms that the current user is a participant of.
     */
    private fun listenToRooms() {
        val userId = auth.currentUser?.uid ?: run {
            _error.value = "User not authenticated"
            return
        }

        // Mark loading as active.
        _isLoading.value = true

        val roomsRef = firestore.collection("rooms")
        val query = roomsRef
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)

        // Attach a real-time snapshot listener.
        roomsListenerRegistration = query.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                _error.value = "Failed to load rooms: ${exception.message}"
                _isLoading.value = false
                return@addSnapshotListener
            }

            if (snapshot != null) {
                // Launch a coroutine to fetch and process room data.
                viewModelScope.launch {
                    val roomsList = snapshot.documents.mapNotNull { roomDoc ->
                        val data = roomDoc.data ?: return@mapNotNull null

                        @Suppress("UNCHECKED_CAST")
                        val participants =
                            data["participants"] as? List<String> ?: return@mapNotNull null

                        // Identify the "other" user in the room.
                        val otherUserId =
                            participants.firstOrNull { it != userId } ?: return@mapNotNull null

                        // Fetch the other user's data from the cache or from Firestore if not already fetched.
                        val user = userCache[otherUserId] ?: try {
                            val userDoc = firestore.collection("users")
                                .document(otherUserId)
                                .get()
                                .await()
                            val userData = userDoc.data
                            val newUser = User(
                                userId = otherUserId,
                                username = userData?.get("username") as? String ?: "Unknown User",
                                profileUrl = userData?.get("profileUrl") as? String ?: "",
                                deviceToken = userData?.get("deviceToken") as? String ?: ""
                            )
                            userCache[otherUserId] = newUser
                            newUser
                        } catch (e: Exception) {
                            logger(
                                "homePack",
                                message = e.message.toString()
                            )
                            null
                        } ?: return@mapNotNull null

                        RoomData(
                            roomId = roomDoc.id,
                            lastMessage = data["lastMessage"] as? String ?: "",
                            lastMessageTimestamp = data["lastMessageTimestamp"] as? Timestamp,
                            lastMessageSenderId = (data["lastMessageSenderId"] as? String).toString(),
                            otherParticipant = user
                        )
                    }
                    if (roomsList.isNotEmpty()) {
                        _rooms.value = roomsList
                        cacheHelper.saveRooms(roomsList)
                    }
                    _isLoading.value = false
                }
            } else {
                _isLoading.value = false
            }
        }
    }

    /**
     * Retry loading rooms by reattaching the listener.
     */
    fun retryLoadRooms() {
        // Remove any existing listener before retrying.
        roomsListenerRegistration?.remove()
        listenToRooms()
    }

    override fun onCleared() {
        // Clean up the listener when the ViewModel is cleared.
        roomsListenerRegistration?.remove()
        super.onCleared()
    }
}
