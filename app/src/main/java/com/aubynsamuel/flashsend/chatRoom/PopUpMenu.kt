package com.aubynsamuel.flashsend.chatRoom

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PopUpMenu(
    expanded: Boolean,
    onDismiss: () -> Unit, // Callback to update state in the parent
    modifier: Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss, // Call the dismiss callback here
        modifier = modifier
    ) {
        DropdownMenuItem(
            text = { Text("Option 1") },
            onClick = {
                // Perform action for Option 1, then dismiss the menu
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text("Option 2") },
            onClick = {
                // Perform action for Option 2, then dismiss the menu
                onDismiss()
            }
        )
    }
}

