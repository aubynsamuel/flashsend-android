package com.aubynsamuel.flashsend.settings.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.runtime.Composable
import com.aubynsamuel.flashsend.core.domain.model.SettingsState
import com.aubynsamuel.flashsend.settings.presentation.viewmodels.SettingsViewModel

@Composable
fun AppearanceSection(
    state: SettingsState,
    viewModel: SettingsViewModel,
) {
    SectionWrapper(title = "Appearance", icon = Icons.Default.Palette) {
        DarkModeSelector(
            currentMode = state.themeMode,
            onModeSelected = viewModel::updateThemeMode
        )

        FontSizeSelector(
            currentSize = state.fontSize,
            onSizeChanged = viewModel::updateFontSize
        )
    }
}