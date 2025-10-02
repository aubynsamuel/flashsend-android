package com.aubynsamuel.flashsend.chat.presentation.components.messageTypes

import android.util.Log
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.aubynsamuel.flashsend.chat.presentation.utils.vibrateDevice
import com.aubynsamuel.flashsend.core.data.MediaCacheManager
import com.aubynsamuel.flashsend.core.domain.model.ChatMessage
import com.aubynsamuel.flashsend.core.presentation.navigation.FullScreenImageViewerDC
import com.aubynsamuel.flashsend.navigation.LocalChatRoomAnimatedVisibilityScope
import com.aubynsamuel.flashsend.navigation.LocalNavController
import com.aubynsamuel.flashsend.navigation.LocalSharedTransitionScope

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ImageMessage(
    message: ChatMessage,
    isFromMe: Boolean,
    fontSize: Int = 16,
    showPopUp: () -> Unit,
) {
    val tag = "ImageMessage"
    message.image?.let { imageUrl ->
        val context = LocalContext.current
        var mediaUri by remember { mutableStateOf(imageUrl.toUri()) }
        val sharedTransitionScope = LocalSharedTransitionScope.current
        val navController = LocalNavController.current

        LaunchedEffect(imageUrl) {
            val cachedUri = MediaCacheManager.getMediaUri(context, imageUrl)
            Log.d(tag, "Retrieved cached image URI: $cachedUri")
            mediaUri = cachedUri
        }

        Column {
            with(sharedTransitionScope) {
                AsyncImage(
                    model = mediaUri,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    vibrateDevice(context)
                                    showPopUp()
                                },
                                onTap = { navController.navigate(FullScreenImageViewerDC(mediaUri.toString())) })
                        }
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "image/$mediaUri"),
                            animatedVisibilityScope = LocalChatRoomAnimatedVisibilityScope.current
                        )
                        .heightIn(min = 30.dp, max = 250.dp)
                        .fillMaxWidth(),
                    contentDescription = "Image message",
                    contentScale = ContentScale.FillWidth
                )
            }

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
        }
    }
}