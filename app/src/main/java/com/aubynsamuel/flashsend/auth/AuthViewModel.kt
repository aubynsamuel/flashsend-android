package com.aubynsamuel.flashsend.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow(repository.isUserLoggedIn())
    val authState: StateFlow<Boolean> = _authState

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            val result = repository.signUp(email, password)
            result.onSuccess {
                _authState.value = true
                _message.value = it
            }.onFailure {
                _message.value = it.message
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val result = repository.login(email, password)
            result.onSuccess {
                _authState.value = true
                _message.value = it
            }.onFailure {
                _message.value = it.message
            }
        }
    }

    fun logout() {
        repository.logout()
        _authState.value = false
    }
}
