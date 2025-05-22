package com.aubynsamuel.flashsend.settings.domain

import com.aubynsamuel.flashsend.core.model.SettingsState
import com.aubynsamuel.flashsend.core.model.ThemeMode
import javax.inject.Inject

class UpdateThemeModeUseCase @Inject constructor(
    private val saveSettingsUseCase: SaveSettingsUseCase,
) {
    suspend operator fun invoke(currentSettings: SettingsState, newThemeMode: ThemeMode) {
        val updatedSettings = currentSettings.copy(themeMode = newThemeMode)
        saveSettingsUseCase(updatedSettings)
    }
}