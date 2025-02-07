package com.aubynsamuel.flashsend.chatRoom

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
fun PopUpMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier,
    dropItems: List<DropMenu>
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        dropItems.forEach { dropItem ->
            DropdownMenuItem(
                leadingIcon = { Icon(Icons.Default.Key, contentDescription = "") },
                text = { Text(dropItem.text, fontSize = 16.sp) },
                onClick = {
                    dropItem.onClick
                }
            )
        }
    }
}

data class DropMenu(
    val text: String = "",
    val onClick: () -> Unit,
    val iconName: String? = ""
)

//@Preview
//@Composable
//fun PrevPopUpMenu() {
//    PopUpMenu(
//        expanded = true,
//        onDismiss = { },
//        modifier = Modifier
//    )
//
//}
