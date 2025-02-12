package com.aubynsamuel.flashsend.chatRoom.messageTypes

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import coil.compose.AsyncImage
import com.aubynsamuel.flashsend.functions.ChatMessage
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun ImageMessage(message: ChatMessage, isFromMe: Boolean) {
    var isExpanded by remember { mutableStateOf(false) }

    message.image?.let { imageUrl ->
        Column {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Image message",
                modifier = Modifier
                    .width(250.dp)
                    .height(350.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { isExpanded = true },
                contentScale = ContentScale.Crop
            )
            if (message.content != "") {
                Text(
                    text = message.content,
                    color = if (isFromMe) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .padding(horizontal = 5.dp)
                )
            }

            if (isExpanded) {
                FullScreenImageViewer(imageUrl) { isExpanded = false }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenImageViewer(imageUrl: String, onDismiss: () -> Unit) {
    var dragOffset by remember { mutableFloatStateOf(0f) }
    // Define a threshold (in pixels) beyond which the viewer will dismiss
    val dragThreshold = 200f

    // Compute the opacity based on the drag distance.
    // When dragOffset == 0, alpha is 1; when dragOffset reaches the threshold, alpha is 0.5.
    val computedAlpha by animateFloatAsState(
        targetValue = (1f - (abs(dragOffset) / dragThreshold)).coerceIn(0.5f, 1f)
    )

    Popup(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = computedAlpha))
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _: PointerInputChange, dragAmount: Float ->
                            dragOffset += dragAmount
                        },
                        onDragEnd = {
                            if (abs(dragOffset) > dragThreshold) {
                                // Dismiss the viewer if drag exceeds threshold
                                onDismiss()
                            } else {
                                // Otherwise, animate the image back to its original position
                                dragOffset = 0f
                            }
                        }
                    )
                }
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Expanded Image",
                modifier = Modifier
                    .fillMaxSize()
                    // Move the image according to the drag offset
                    .offset { IntOffset(x = 0, y = dragOffset.roundToInt()) }
                    .alpha(computedAlpha),
                contentScale = ContentScale.Fit
            )
        }
    }
}