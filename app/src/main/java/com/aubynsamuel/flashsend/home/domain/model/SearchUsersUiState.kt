package com.aubynsamuel.flashsend.home.domain.model

import com.aubynsamuel.flashsend.core.model.User

data class SearchUsersUiState(
    val searchText: String = "",
    val filteredUsers: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String = "Search users",
)