package com.aubynsamuel.flashsend.settings.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aubynsamuel.flashsend.core.model.SettingsState
import com.aubynsamuel.flashsend.core.model.ThemeMode
import com.aubynsamuel.flashsend.settings.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    init {
        loadSavedSettings()
    }

    private fun loadSavedSettings() {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { savedSettings ->
                _settingsState.update { savedSettings }
            }
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        _settingsState.update { it.copy(themeMode = mode) }
        saveSettings()
    }

    fun updateFontSize(fontSize: Int) {
        _settingsState.update { it.copy(fontSize = fontSize) }
        saveSettings()
    }

    fun resetAllSettings() {
        _settingsState.update { SettingsState() }
        saveSettings()
    }

    private fun saveSettings() {
        viewModelScope.launch {
            settingsRepository.saveSettings(settingsState.value)
        }
    }
}