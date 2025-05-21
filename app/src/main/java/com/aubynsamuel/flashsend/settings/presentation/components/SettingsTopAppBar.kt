package com.aubynsamuel.flashsend.settings.presentation.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopAppBar(
    onBack: () -> Unit,
    onReset: () -> Unit,
) {
    TopAppBar(
        modifier = Modifier.height(80.dp),
        title = { Text("Settings", modifier = Modifier.padding(top = 10.dp)) },
//        navigationIcon = {
//            IconButton(onClick = onBack) {
//                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
//            }
//        },
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