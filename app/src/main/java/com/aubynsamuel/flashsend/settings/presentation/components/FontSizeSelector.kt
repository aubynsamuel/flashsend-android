package com.aubynsamuel.flashsend.settings.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aubynsamuel.flashsend.core.data.mock.messageExample

@Composable
fun FontSizeSelector(
    currentSize: Int,
    onSizeChanged: (Int) -> Unit,
) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(imageVector = Icons.Default.TextFields, contentDescription = "")
            Column {
                Text(
                    text = "Font Size: ",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (currentSize == 16) "Normal" else
                        "${currentSize}sp",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Slider(
            value = currentSize.toFloat(),
            onValueChange = { onSizeChanged(it.toInt()) },
            valueRange = 12f..20f,
            steps = 3,
        )
        Text(text = "Live Demo")
        DemoMessage(
            message = messageExample, isFromMe = true,
            modifier = Modifier,
            fontSize = currentSize
        )
    }
}