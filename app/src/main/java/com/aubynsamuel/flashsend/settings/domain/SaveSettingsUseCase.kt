package com.aubynsamuel.flashsend.settings.domain

import com.aubynsamuel.flashsend.core.model.SettingsState
import com.aubynsamuel.flashsend.settings.data.SettingsRepository
import javax.inject.Inject

class SaveSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(settings: SettingsState) {
        settingsRepository.saveSettings(settings)
    }
}