package com.aubynsamuel.flashsend.chatRoom

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp

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

data class DropMenu(
    val text: String = "",
    val onClick: () -> Unit,
    val icon: ImageVector? = null
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
