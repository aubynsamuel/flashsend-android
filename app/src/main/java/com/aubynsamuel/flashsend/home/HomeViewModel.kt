package com.aubynsamuel.flashsend.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _rooms = MutableStateFlow<List<RoomData>>(emptyList())
    val rooms: StateFlow<List<RoomData>> = _rooms

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadRooms()
    }

    private fun loadRooms() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = "3xhYuMeHR5PrDKG1xkbBtxWEpT32" // Replace with actual user ID
            val roomsRef = FirebaseFirestore.getInstance().collection("rooms")

            roomsRef.whereArrayContains("participants", userId)
                .orderBy("lastMessageTimestamp")
                .get()
                .addOnSuccessListener { snapshot ->
                    val roomsList = snapshot.documents.mapNotNull { roomDoc ->
                        val data = roomDoc.data ?: return@mapNotNull null
                        val otherUserId = (data["participants"] as List<*>)
                            .filterIsInstance<String>()
                            .firstOrNull { it != userId } ?: return@mapNotNull null

                        val user = User(
                            userId = otherUserId,
                            username = "User $otherUserId",
                            profileUrl = "",
                            otherUsersDeviceToken = ""
                        )

                        RoomData(
                            roomId = roomDoc.id,
                            lastMessage = data["lastMessage"] as? String,
                            lastMessageTimestamp = data["lastMessageTimestamp"] as? Long,
                            lastMessageSenderId = data["lastMessageSenderId"] as? String,
                            otherParticipant = user
                        )
                    }
                    _rooms.value = roomsList
                    _isLoading.value = false
                }
                .addOnFailureListener {
                    _isLoading.value = false
                }
        }
    }
}
