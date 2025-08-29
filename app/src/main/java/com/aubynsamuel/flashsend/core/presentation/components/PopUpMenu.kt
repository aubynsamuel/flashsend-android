package com.aubynsamuel.flashsend.core.presentation.components

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.aubynsamuel.flashsend.core.domain.model.DropMenu

@Composable
fun PopUpMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier,
    dropItems: List<DropMenu>,
    reactions: @Composable () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        reactions()
        dropItems.forEach { dropItem ->
            DropdownMenuItem(
                leadingIcon = {
                    dropItem.icon?.let {
                        Icon(it, contentDescription = "")
                    }
                },
                text = { Text(dropItem.text, fontSize = 14.sp) },
                onClick = {
                    dropItem.onClick()
                    onDismiss()
                }
            )
        }
    }
}