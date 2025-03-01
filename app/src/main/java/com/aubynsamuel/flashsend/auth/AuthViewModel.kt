package com.aubynsamuel.flashsend.auth

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aubynsamuel.flashsend.functions.MediaCacheManager
import com.aubynsamuel.flashsend.functions.NewUser
import com.aubynsamuel.flashsend.home.CacheHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class AuthViewModel(private val repository: AuthRepository, context: Context) :
    ViewModel() {
    private val appContext = context.applicationContext

    val appCredentialsManager = AppCredentialsManager(appContext)

    private val cacheHelper = CacheHelper(context = context)

    private val _authState = MutableStateFlow(repository.isUserLoggedIn())
    val authState: StateFlow<Boolean> = _authState

    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn: StateFlow<Boolean> = _isLoggingIn

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val firebase = FirebaseFirestore.getInstance()

    suspend fun saveCredentials(email: String, password: String) {
        try {
            val credentialSaved = appCredentialsManager.registerPassword(email, password)
            if (!credentialSaved) {
                Log.d(
                    "AuthViewModel",
                    "Credentials were not saved - this may be normal if user declined"
                )
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error saving credentials: ${e.message}")
        }
    }

    fun signUp(email: String, password: String) {
        _isLoggingIn.value = true
        viewModelScope.launch {
            val result = repository.signUp(email, password)
            result.onSuccess {
                _authState.value = true
                _message.value = it
                _isLoggingIn.value = false
                saveCredentials(email, password)
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
                    val cachedUri =
                        MediaCacheManager.getMediaUri(appContext, originalProfileUrl)
                    val user = NewUser(
                        userId = userId,
                        username = userDataMap["username"] as? String ?: "Unknown User",
                        profileUrl = cachedUri.toString(),
                        deviceToken = userDataMap["deviceToken"] as? String ?: "",
                        email = userDataMap["email"] as? String ?: ""
                    )
                    CurrentUser.updateUser(user)
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
                saveCredentials(email, password)
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
        cacheHelper.clearRooms()
    }

    fun clearMessage() {
        _message.value = null
    }
}
