package com.aubynsamuel.flashsend.chatRoom

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun PopUpMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        DropdownMenuItem(
            text = { Text("Option 1") },
            onClick = {
                onDismiss()
            }
        )
        DropdownMenuItem(
            text = { Text("Option 2") },
            onClick = {
                onDismiss()
            }
        )
    }
}

@Preview
@Composable
fun PrevPopUpMenu() {
    PopUpMenu(
        expanded = true,
        onDismiss = { },
        modifier = Modifier
    )

}
