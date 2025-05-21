package com.aubynsamuel.flashsend.core.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ConnectivityViewModel @Inject constructor(
    private val connectivityObserver: NetworkConnectivityObserver,
) : ViewModel() {

    private val _connectivityStatus =
        MutableStateFlow<ConnectivityStatus>(ConnectivityStatus.Unavailable)
    val connectivityStatus: StateFlow<ConnectivityStatus> = _connectivityStatus

    init {
        observeConnectivity()
    }

    private fun observeConnectivity() {
        connectivityObserver.observe()
            .onEach { status ->
                _connectivityStatus.value = status
            }
            .launchIn(viewModelScope)
    }
}