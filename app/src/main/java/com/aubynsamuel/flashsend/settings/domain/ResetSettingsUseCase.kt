package com.aubynsamuel.flashsend.settings.domain


import com.aubynsamuel.flashsend.core.model.SettingsState
import javax.inject.Inject

class ResetSettingsUseCase @Inject constructor(
    private val saveSettingsUseCase: SaveSettingsUseCase,
) {
    suspend operator fun invoke() {
        val defaultSettings = SettingsState()
        saveSettingsUseCase(defaultSettings)
    }
}