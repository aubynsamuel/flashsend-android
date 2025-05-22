package com.aubynsamuel.flashsend.settings.domain

import com.aubynsamuel.flashsend.core.model.SettingsState
import com.aubynsamuel.flashsend.settings.data.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<SettingsState> = settingsRepository.settingsFlow
}