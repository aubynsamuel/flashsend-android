package com.aubynsamuel.flashsend.home.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aubynsamuel.flashsend.core.domain.logger
import com.aubynsamuel.flashsend.home.data.RoomsCache
import com.aubynsamuel.flashsend.home.domain.ListenToRoomsUseCase
import com.aubynsamuel.flashsend.home.domain.model.HomeUiState
import com.aubynsamuel.flashsend.home.domain.usecase.GetFCMTokenUseCase
import com.aubynsamuel.flashsend.home.domain.usecase.GetUnreadMessagesUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUnreadMessagesUseCase: GetUnreadMessagesUseCase,
    private val getFCMTokenUseCase: GetFCMTokenUseCase,
    private val listenToRoomsUseCase: ListenToRoomsUseCase,
    context: Context,
) : ViewModel() {
    private val appContext = context.applicationContext
    private val _uiState = MutableStateFlow(HomeUiState())
    private val tag = "HomeViewModel"
    private val cacheHelper = RoomsCache(context = context)
    private val auth = FirebaseAuth.getInstance()
    private var roomsListenerJob = viewModelScope.launch { } // Initialize an empty job
    val uiState: StateFlow<HomeUiState> = _uiState

    private fun loadCachedRooms() {
        viewModelScope.launch {
            val cachedRooms = cacheHelper.loadRooms()
            _uiState.update { it.copy(rooms = cachedRooms) }
            logger(tag, "Cached rooms loaded ${cachedRooms.size}")
        }
    }

    init {
        if (auth.currentUser != null) {
            loadCachedRooms()
            listenToRooms()
        } else {
            _uiState.update { it.copy(error = "User not authenticated") }
        }
    }

    fun getUnreadMessages(
        roomId: String,
        otherUserId: String,
        callBack: (value: Int) -> Unit,
    ) {
        getUnreadMessagesUseCase(roomId, otherUserId, callBack)
    }

    fun getFCMToken(callBack: (value: String) -> Unit) {
        getFCMTokenUseCase(callBack)
    }

    private fun listenToRooms() {
        val userId = auth.currentUser?.uid ?: run {
            _uiState.update { it.copy(error = "User not authenticated") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        roomsListenerJob = viewModelScope.launch {
            listenToRoomsUseCase(userId).collectLatest { result ->
                result.onSuccess { roomsList ->
                    _uiState.update { it.copy(rooms = roomsList, isLoading = false) }
                    cacheHelper.saveRooms(roomsList)
                }
                result.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            error = "Failed to load rooms: ${error.message}",
                            isLoading = false
                        )
                    }
                    logger(tag, "Failed to load rooms: ${error.message}")
                }
            }
        }
    }

    fun retryLoadRooms() {
        roomsListenerJob.cancel()
        listenToRooms()
    }

    override fun onCleared() {
        roomsListenerJob.cancel()
        super.onCleared()
    }
}