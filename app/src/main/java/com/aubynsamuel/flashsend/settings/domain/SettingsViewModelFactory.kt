package com.aubynsamuel.flashsend.settings.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aubynsamuel.flashsend.settings.data.SettingsRepository
import com.aubynsamuel.flashsend.settings.presentation.viewmodels.SettingsViewModel

class SettingsViewModelFactory(
    private val repository: SettingsRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}