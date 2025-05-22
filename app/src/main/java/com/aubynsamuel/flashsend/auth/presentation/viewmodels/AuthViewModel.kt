package com.aubynsamuel.flashsend.auth.presentation.viewmodels

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aubynsamuel.flashsend.auth.data.AppCredentialsManager
import com.aubynsamuel.flashsend.auth.domain.GetUserDataUseCase
import com.aubynsamuel.flashsend.auth.domain.GetUserIdUseCase
import com.aubynsamuel.flashsend.auth.domain.IsUserLoggedInUseCase
import com.aubynsamuel.flashsend.auth.domain.LoginUseCase
import com.aubynsamuel.flashsend.auth.domain.LogoutUseCase
import com.aubynsamuel.flashsend.auth.domain.ResetPasswordUseCase
import com.aubynsamuel.flashsend.auth.domain.SignUpUseCase
import com.aubynsamuel.flashsend.auth.domain.UpdateUserDocumentUseCase
import com.aubynsamuel.flashsend.core.state.CurrentUser
import com.aubynsamuel.flashsend.home.data.RoomsCache
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val loginUseCase: LoginUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val updateUserDocumentUseCase: UpdateUserDocumentUseCase,
    isUserLoggedInUseCase: IsUserLoggedInUseCase,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getUserDataUseCase: GetUserDataUseCase,
    context: Context,
) : ViewModel() {
    private val tag = "AuthViewModel"
    private val appContext = context.applicationContext
    private val cacheHelper = RoomsCache(context = context)
    private val _authState = MutableStateFlow(isUserLoggedInUseCase())
    private val _isLoggingIn = MutableStateFlow(false)
    private val _message = MutableStateFlow<String?>(null)
    val appCredentialsManager = AppCredentialsManager(appContext)
    val authState: StateFlow<Boolean> = _authState
    val isLoggingIn: StateFlow<Boolean> = _isLoggingIn
    val message: StateFlow<String?> = _message

    suspend fun saveCredentials(email: String, password: String) {
        try {
            appCredentialsManager.registerPassword(email, password)
        } catch (e: Exception) {
            Log.e(tag, "Error saving credentials: ${e.message}")
        }
    }

    fun signUp(email: String, password: String) {
        _isLoggingIn.value = true
        viewModelScope.launch {
            val result = signUpUseCase(email, password)
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
            val result = updateUserDocumentUseCase(newData)
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
            val userId = getUserIdUseCase()
            if (userId == null) {
                _message.value = "User not authenticated"
                return@launch
            }
            val result = getUserDataUseCase(userId)
            result.onSuccess { user ->
                user?.let {
                    CurrentUser.updateUser(it)
                } ?: run {
                    _message.value = "User data not found"
                }
            }.onFailure { error ->
                _message.value = "Failed to load user data: ${error.message}"
            }
        }
    }


    fun login(email: String, password: String) {
        _isLoggingIn.value = true
        viewModelScope.launch {
            val result = loginUseCase(email, password)
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
            val result = resetPasswordUseCase(email)
            result.onSuccess {
                _message.value = it
            }.onFailure {
                _message.value = it.message
            }
        }
    }

    fun logout() {
        logoutUseCase()
        _authState.value = false
        cacheHelper.clearRooms()
    }

    fun clearMessage() {
        _message.value = null
    }
}