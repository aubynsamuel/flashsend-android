package com.aubynsamuel.flashsend.settings.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aubynsamuel.flashsend.core.model.SettingsState
import com.aubynsamuel.flashsend.core.model.ThemeMode
import com.aubynsamuel.flashsend.settings.domain.GetSettingsUseCase
import com.aubynsamuel.flashsend.settings.domain.ResetSettingsUseCase
import com.aubynsamuel.flashsend.settings.domain.UpdateFontSizeUseCase
import com.aubynsamuel.flashsend.settings.domain.UpdateThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val getSettingsUseCase: GetSettingsUseCase,
    val updateThemeModeUseCase: UpdateThemeModeUseCase,
    val updateFontSizeUseCase: UpdateFontSizeUseCase,
    val resetSettingsUseCase: ResetSettingsUseCase,
) : ViewModel() {

    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    init {
        loadSavedSettings()
    }

    private fun loadSavedSettings() {
        viewModelScope.launch {
            getSettingsUseCase().collect { savedSettings ->
                _settingsState.update { savedSettings }
            }
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            updateThemeModeUseCase(
                currentSettings = _settingsState.value,
                newThemeMode = mode
            )
            // Update local state for immediate UI feedback
            _settingsState.update { it.copy(themeMode = mode) }
        }
    }

    fun updateFontSize(fontSize: Int) {
        viewModelScope.launch {
            updateFontSizeUseCase(
                currentSettings = _settingsState.value,
                newFontSize = fontSize
            )
            // Update local state for immediate UI feedback
            _settingsState.update { it.copy(fontSize = fontSize) }
        }
    }

    fun resetAllSettings() {
        viewModelScope.launch {
            resetSettingsUseCase()
            // Update local state for immediate UI feedback
            _settingsState.update { SettingsState() }
        }
    }
}