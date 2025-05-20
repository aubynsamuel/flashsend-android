package com.aubynsamuel.flashsend.chatRoom.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScrollToBottom(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        shape = RoundedCornerShape(30.dp),
        modifier = Modifier
            .padding(bottom = 80.dp, end = 5.dp)
            .size(35.dp)
    ) {
        Icon(
            Icons.Default.KeyboardArrowDown,
            contentDescription = "Scroll to bottom action button",
        )
    }
}
