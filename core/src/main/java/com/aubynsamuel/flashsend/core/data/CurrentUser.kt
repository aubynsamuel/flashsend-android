package com.aubynsamuel.flashsend.core.data

import com.aubynsamuel.flashsend.core.domain.model.NewUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object CurrentUser {
    private val _userData = MutableStateFlow<NewUser?>(null)
    val userData: StateFlow<NewUser?> = _userData

    fun updateUser(newUser: NewUser?) {
        _userData.value = newUser
    }
}