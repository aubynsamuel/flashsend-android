package com.aubynsamuel.flashsend.home.domain.model

import com.aubynsamuel.flashsend.core.model.RoomData

data class HomeUiState(
    val rooms: List<RoomData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)