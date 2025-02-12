package com.aubynsamuel.flashsend.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aubynsamuel.flashsend.functions.SettingsState
import com.aubynsamuel.flashsend.functions.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    init {
        loadSavedSettings()
    }

    private fun loadSavedSettings() {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { savedSettings ->
                _uiState.update { savedSettings }
            }
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
        saveSettings()
    }

    fun updateFontSize(fontSize: Int) {
        _uiState.update { it.copy(fontSize = fontSize) }
        saveSettings()
    }

    fun resetAllSettings() {
        _uiState.update { SettingsState() }
        saveSettings()
    }

    private fun saveSettings() {
        viewModelScope.launch {
            settingsRepository.saveSettings(uiState.value)
        }
    }
}
