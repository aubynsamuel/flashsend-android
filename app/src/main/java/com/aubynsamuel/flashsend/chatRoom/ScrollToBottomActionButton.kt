package com.aubynsamuel.flashsend.chatRoom

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ScrollToBottom(onClick: () -> Unit, modifier: Modifier) {
    FloatingActionButton(onClick = onClick, modifier = modifier) {
        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Scroll to bottom action button")
    }
}
