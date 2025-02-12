package com.aubynsamuel.flashsend.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aubynsamuel.flashsend.auth.AuthViewModel
import com.aubynsamuel.flashsend.functions.SettingsState
import com.aubynsamuel.flashsend.functions.ThemeMode
import com.aubynsamuel.flashsend.mockData.messageExample

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel, navController: NavController, authViewModel: AuthViewModel,

    ) {
    val userData by authViewModel.userData.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
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
                    state = uiState, onEditProfile = { navController.navigate("editProfile") },
                    username = userData?.username ?: ""
                )
            }
            item { AppearanceSection(uiState, viewModel) }
//            item { NotificationsSection(uiState, viewModel) }
//            item { PrivacySection(uiState, viewModel) }
//            item { AboutSection(uiState, onNavigateToTerms, onContactSupport) }
        }
    }
}

// Profile Section
@Composable
private fun ProfileSection(
    state: SettingsState,
    onEditProfile: () -> Unit,
    username: String
) {
    SettingsSection(title = "Profile", icon = Icons.Default.Person) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = username,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = state.userStatus,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(onClick = onEditProfile) {
                Text("Edit Profile")
            }
        }
    }
}

// Appearance Section
@Composable
private fun AppearanceSection(
    state: SettingsState,
    viewModel: SettingsViewModel
) {
    SettingsSection(title = "Appearance", icon = Icons.Default.Palette) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DarkModeSelector(
    currentMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("System Default", "Light", "Dark")

    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = "Theme: ${options[currentMode.ordinal]}",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEachIndexed { index, selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            onModeSelected(ThemeMode.entries[index])
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FontSizeSelector(
    currentSize: Int,
    onSizeChanged: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Font Size: ",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (currentSize == 16) "Normal" else
                    "${currentSize}sp",
                color = MaterialTheme.colorScheme.primary
            )
        }

        Slider(
            value = currentSize.toFloat(),
            onValueChange = { onSizeChanged(it.toInt()) },
            valueRange = 12f..20f,
            steps = 3,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "Live Demo")
        DemoMessage(
            message = messageExample, isFromMe = true,
            modifier = Modifier,
            fontSize = currentSize
        )
    }
}

// ResetConfirmationDialog.kt
@Composable
fun ResetConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset Settings") },
        text = { Text("Are you sure you want to reset all settings to default?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Reset", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// SettingsTopAppBar.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopAppBar(
    onBack: () -> Unit,
    onReset: () -> Unit
) {
    TopAppBar(
        title = { Text("Settings") },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = onReset) {
                Icon(Icons.Default.RestartAlt, contentDescription = "Reset Settings")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

// SettingsSection.kt
@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        content()
    }
}