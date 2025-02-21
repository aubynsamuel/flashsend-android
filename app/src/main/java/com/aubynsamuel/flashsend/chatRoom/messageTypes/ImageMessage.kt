package com.aubynsamuel.flashsend.chatRoom.messageTypes

import android.net.Uri
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import coil.compose.AsyncImage
import com.aubynsamuel.flashsend.MediaCacheManager
import com.aubynsamuel.flashsend.functions.ChatMessage
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun ImageMessage(message: ChatMessage, isFromMe: Boolean, fontSize: Int = 16) {
    var isExpanded by remember { mutableStateOf(false) }
    message.image?.let { imageUrl ->
        val context = LocalContext.current
        var mediaUri by remember { mutableStateOf(Uri.parse(imageUrl)) }

        // Retrieve the cached URI asynchronously.
        LaunchedEffect(imageUrl) {
            val cachedUri = MediaCacheManager.getMediaUri(context, imageUrl)
            Log.d("ImageMessage", "Retrieved cached image URI: $cachedUri")
            mediaUri = cachedUri
        }

        Column {
            AsyncImage(
                model = mediaUri,
                contentDescription = "Image message",
                modifier = Modifier
//                    .width(250.dp)
                    .heightIn(min = 30.dp, max = 250.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .fillMaxWidth()
                    .clickable { isExpanded = true },
                contentScale = ContentScale.FillWidth
            )
            if (message.content.isNotEmpty()) {
                Text(
                    text = message.content,
                    color = if (isFromMe) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontSize = fontSize.sp,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .padding(horizontal = 5.dp)
                )
            }
            if (isExpanded) {
                FullScreenImageViewer(mediaUri.toString()) { isExpanded = false }
            }
        }
    }
}


@Composable
fun FullScreenImageViewer(imageUri: String, onDismiss: () -> Unit) {
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
                AsyncImage(
                    model = imageUri,
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
}