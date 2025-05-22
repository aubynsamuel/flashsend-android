package com.aubynsamuel.flashsend.core.data.mock

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import com.aubynsamuel.flashsend.chatRoom.presentation.components.DropMenu

@Suppress("unused")
val optionsListExample: List<DropMenu> = listOf(
    DropMenu(
        text = "Copy", onClick = {}, icon = Icons.Default.CopyAll
    ), DropMenu(
        text = "Save", onClick = {}, icon = Icons.Default.Save
    ), DropMenu(
        text = "Delete", onClick = {}, icon = Icons.Default.Delete
    ), DropMenu(
        text = "Edit", onClick = {}, icon = Icons.Default.Edit
    )
)