package com.aubynsamuel.flashsend.chatRoom

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun MessageInput(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val transition =
        updateTransition(targetState = messageText.isNotBlank(), label = "messageTransition")

    val translateX by transition.animateFloat(
        transitionSpec = { tween(200) },
        label = "translationX"
    ) { if (it) 45f else 0f }
    val translate by transition.animateFloat(
        transitionSpec = { tween(200) },
        label = "translationX"
    ) { if (it) 80f else 0f }

    val sendIconScale by transition.animateFloat(
        transitionSpec = { tween(100) },
        label = "sendIconScale"
    ) { if (it) 0.3f else 1f }

    val placeIconScale by transition.animateFloat(
        transitionSpec = { tween(100, delayMillis = 100) },
        label = "placeIconScale"
    ) { if (it) 1f else 0.3f }

    val homeIconAlpha by transition.animateFloat(
        transitionSpec = { tween(150) },
        label = "homeIconAlpha"
    ) { if (it) 0f else 1f }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = messageText,
            onValueChange = onMessageChange,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            placeholder = { Text("Type a message...") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(24.dp),
            singleLine = false,
            maxLines = 5,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Default
            ),
            trailingIcon = {
                Row {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .graphicsLayer {
                                translationX = translate
                            }
                            .clickable(onClick = {})
                    )
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .graphicsLayer {
                                translationX = translateX
                                alpha = homeIconAlpha
                            }
                            .padding(horizontal = 10.dp)
                            .clickable(onClick = {})
                    )
                }
            }
        )

        IconButton(
            onClick = if (messageText.isNotBlank()) onSend else onSend,
            modifier = Modifier
                .size(55.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        ) {
            AnimatedVisibility(visible = messageText.isBlank()) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Mic",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.graphicsLayer {
                        scaleX = sendIconScale
                        scaleY = sendIconScale
                    }
                )
            }

            AnimatedVisibility(visible = messageText.isNotBlank()) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.graphicsLayer {
                        scaleX = placeIconScale
                        scaleY = placeIconScale
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun PrevInputToolBar() {
    MessageInput(messageText = "", onMessageChange = {}, onSend = {})
}