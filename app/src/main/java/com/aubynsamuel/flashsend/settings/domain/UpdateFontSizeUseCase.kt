package com.aubynsamuel.flashsend.settings.domain

import com.aubynsamuel.flashsend.core.model.SettingsState
import javax.inject.Inject

class UpdateFontSizeUseCase @Inject constructor(
    private val saveSettingsUseCase: SaveSettingsUseCase,
) {
    suspend operator fun invoke(currentSettings: SettingsState, newFontSize: Int) {
        val updatedSettings = currentSettings.copy(fontSize = newFontSize)
        saveSettingsUseCase(updatedSettings)
    }
}