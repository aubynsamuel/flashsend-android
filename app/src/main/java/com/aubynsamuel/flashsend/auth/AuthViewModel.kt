package com.aubynsamuel.flashsend.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aubynsamuel.flashsend.MediaCacheManager
import com.aubynsamuel.flashsend.functions.NewUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(private val repository: AuthRepository, context: Context) :
    ViewModel() {
    private val appContext = context.applicationContext

    private val _authState = MutableStateFlow(repository.isUserLoggedIn())
    val authState: StateFlow<Boolean> = _authState

    val userData = MutableStateFlow<NewUser?>(null)

    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn: StateFlow<Boolean> = _isLoggingIn

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val firebase = FirebaseFirestore.getInstance()

    fun signUp(email: String, password: String) {
        _isLoggingIn.value = true
        viewModelScope.launch {
            val result = repository.signUp(email, password)
            result.onSuccess {
                _authState.value = true
                _message.value = it
                _isLoggingIn.value = false

            }.onFailure {
                _isLoggingIn.value = false
                _message.value = it.message
            }
        }
    }

    fun updateUserDocument(newData: Map<String, Any>) {
        _isLoggingIn.value = true
        viewModelScope.launch {
            val result = repository.updateUserDocument(newData)
            result.onSuccess {
                _message.value = it
            }.onFailure {
                _message.value = it.message
            }
            _isLoggingIn.value = false
        }
    }

    fun loadUserData() {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                _message.value = "User not authenticated"
                return@launch
            }

            try {
                val document = firebase.collection("users").document(userId).get().await()
                val userDataMap = document.data
                if (userDataMap != null) {
                    val originalProfileUrl = userDataMap["profileUrl"] as? String ?: ""
                    // Get the locally cached URI for the profile image.
                    val cachedUri = MediaCacheManager.getMediaUri(appContext, originalProfileUrl)
                    val user = NewUser(
                        userId = userId,
                        username = userDataMap["username"] as? String ?: "Unknown User",
                        profileUrl = cachedUri.toString(),
                        deviceToken = userDataMap["deviceToken"] as? String ?: "",
                        email = userDataMap["email"] as? String ?: ""
                    )
                    userData.value = user
                } else {
                    _message.value = "User data not found"
                }
            } catch (e: Exception) {
                _message.value = "Failed to load user data: ${e.message}"
            }
        }
    }

    fun login(email: String, password: String) {
        _isLoggingIn.value = true
        viewModelScope.launch {
            val result = repository.login(email, password)
            result.onSuccess {
                _authState.value = true
                _message.value = it
                _isLoggingIn.value = false
            }.onFailure {
                _message.value = it.message
                _isLoggingIn.value = false
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            val result = repository.resetPassword(email)
            result.onSuccess {
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
