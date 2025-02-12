package com.aubynsamuel.flashsend.functions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ConnectivityViewModel(
    private val connectivityObserver: ConnectivityObserver
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