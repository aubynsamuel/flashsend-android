package com.aubynsamuel.flashsend.settings.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.aubynsamuel.flashsend.core.state.CurrentUser
import com.aubynsamuel.flashsend.settings.presentation.components.AppearanceSection
import com.aubynsamuel.flashsend.settings.presentation.components.ProfileSection
import com.aubynsamuel.flashsend.settings.presentation.components.ResetConfirmationDialog
import com.aubynsamuel.flashsend.settings.presentation.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, navController: NavController) {
    val userData by CurrentUser.userData.collectAsStateWithLifecycle()
    val settingsState by viewModel.settingsState.collectAsState()
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

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
            LargeFlexibleTopAppBar(
                scrollBehavior = scrollBehavior,
                title = { Text("Settings", modifier = Modifier.padding(top = 10.dp)) },
                actions = {
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset Settings")
                    }
                },
                expandedHeight = 150.dp,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
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