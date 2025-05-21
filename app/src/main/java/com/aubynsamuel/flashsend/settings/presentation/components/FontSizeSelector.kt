package com.aubynsamuel.flashsend.settings.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aubynsamuel.flashsend.core.data.mock.messageExample

@Composable
fun FontSizeSelector(
    currentSize: Int,
    onSizeChanged: (Int) -> Unit,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Font Size: ",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (currentSize == 16) "Normal" else
                    "${currentSize}sp",
                color = MaterialTheme.colorScheme.primary
            )
        }

        Slider(
            value = currentSize.toFloat(),
            onValueChange = { onSizeChanged(it.toInt()) },
            valueRange = 12f..20f,
            steps = 3,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "Live Demo")
        DemoMessage(
            message = messageExample, isFromMe = true,
            modifier = Modifier,
            fontSize = currentSize
        )
    }
}