package com.aubynsamuel.flashsend.chatRoom.messageTypes

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aubynsamuel.flashsend.chatRoom.vibrateDevice
import com.aubynsamuel.flashsend.functions.ChatMessage
import com.aubynsamuel.flashsend.functions.MediaCacheManager

@Composable
fun ImageMessage(
    message: ChatMessage, isFromMe: Boolean, fontSize: Int = 16, showPopUp: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    message.image?.let { imageUrl ->
        val context = LocalContext.current
        var mediaUri by remember { mutableStateOf(Uri.parse(imageUrl)) }

        LaunchedEffect(imageUrl) {
            val cachedUri = MediaCacheManager.getMediaUri(context, imageUrl)
            Log.d("ImageMessage", "Retrieved cached image URI: $cachedUri")
            mediaUri = cachedUri
        }

        Column {
            AsyncImage(
                model = mediaUri, contentDescription = "Image message", modifier = Modifier
                    .heightIn(min = 30.dp, max = 250.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)
                    )
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(onLongPress = {
                            vibrateDevice(context)
                            showPopUp()
                        }, onTap = { isExpanded = true })
                    },
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
                    lineHeight = getLineHeight(fontSize).sp,
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