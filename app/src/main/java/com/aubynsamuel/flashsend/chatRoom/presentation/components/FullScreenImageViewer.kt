package com.aubynsamuel.flashsend.chatRoom.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import coil.compose.AsyncImage
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun FullScreenImageViewer(imageUri: String, onDismiss: () -> Unit) {
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val dragThreshold = 200f

    val computedAlpha by animateFloatAsState(
        targetValue = (1f - (abs(dragOffset) / dragThreshold)).coerceIn(0.5f, 1f)
    )

    Popup(onDismissRequest = onDismiss) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = computedAlpha))
            .pointerInput(Unit) {
                detectVerticalDragGestures(onVerticalDrag = { _: PointerInputChange, dragAmount: Float ->
                    dragOffset += dragAmount
                }, onDragEnd = {
                    if (abs(dragOffset) > dragThreshold) {
                        onDismiss()
                    } else {
                        dragOffset = 0f
                    }
                })
            }) {
            Column {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close Button",
                    modifier = Modifier
                        .clickable(onClick = { onDismiss() })
                        .size(40.dp)
                        .align(Alignment.End)
                        .absolutePadding(right = 15.dp, top = 10.dp)
                        .alpha(computedAlpha)
                        .offset { IntOffset(x = 0, y = dragOffset.roundToInt()) },
                    tint = MaterialTheme.colorScheme.onBackground
                )
                AsyncImage(model = imageUri,
                    contentDescription = "Expanded Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset(x = 0, y = dragOffset.roundToInt()) }
                        .alpha(computedAlpha),
                    contentScale = ContentScale.Fit)
            }
        }
    }
}