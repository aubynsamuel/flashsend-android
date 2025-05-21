package com.aubynsamuel.flashsend.settings.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.aubynsamuel.flashsend.core.state.CurrentUser
import com.aubynsamuel.flashsend.settings.presentation.components.AppearanceSection
import com.aubynsamuel.flashsend.settings.presentation.components.ProfileSection
import com.aubynsamuel.flashsend.settings.presentation.components.ResetConfirmationDialog
import com.aubynsamuel.flashsend.settings.presentation.components.SettingsTopAppBar
import com.aubynsamuel.flashsend.settings.presentation.viewmodels.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, navController: NavController) {
    val userData by CurrentUser.userData.collectAsStateWithLifecycle()
    val settingsState by viewModel.settingsState.collectAsState()
    var showResetDialog by rememberSaveable { mutableStateOf(false) }

    if (showResetDialog) {
        ResetConfirmationDialog(
            onConfirm = {
                viewModel.resetAllSettings()
                showResetDialog = false
            },
            onDismiss = { showResetDialog = false }
        )
    }

    Scaffold(
        topBar = {
            SettingsTopAppBar(
                onBack = { navController.popBackStack() },
                onReset = { showResetDialog = true }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(vertical = 10.dp),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            item {
                ProfileSection(
                    state = settingsState,
                    onEditProfile = { navController.navigate("editProfile") },
                    username = userData?.username ?: ""
                )
            }
            item { AppearanceSection(settingsState, viewModel) }
//            item { NotificationsSection(settingsState, viewModel) }
//            item { PrivacySection(settingsState, viewModel) }
//            item { AboutSection(settingsState, onNavigateToTerms, onContactSupport) }
        }
    }
}