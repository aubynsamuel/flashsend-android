package com.aubynsamuel.flashsend.settings.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aubynsamuel.flashsend.core.model.SettingsState
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
        Spacer(modifier = Modifier.height(10.dp))

        FontSizeSelector(
            currentSize = state.fontSize,
            onSizeChanged = viewModel::updateFontSize
        )
    }
}