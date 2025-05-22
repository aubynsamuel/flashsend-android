package com.aubynsamuel.flashsend.chatRoom.presentation.components.messageTypes

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.aubynsamuel.flashsend.core.data.MediaCacheManager
import com.aubynsamuel.flashsend.core.data.mock.messageExample
import com.aubynsamuel.flashsend.core.model.ChatMessage
import kotlinx.coroutines.delay

@Composable
fun AudioMessage(message: ChatMessage, isFromMe: Boolean, fontSize: Int) {
    val tag = "AudioMessage"
    val context = LocalContext.current
    // Original URL as fallback
    var mediaUri by remember { mutableStateOf(Uri.parse(message.audio)) }

    LaunchedEffect(message.audio) {
        val cachedUri = MediaCacheManager.getMediaUri(context, message.audio.toString())
        Log.d(tag, "Retrieved media URI: $cachedUri")
        mediaUri = cachedUri
    }

    val exoPlayer = remember { ExoPlayer.Builder(context).build() }

    LaunchedEffect(mediaUri) {
        Log.d(tag, "Updating ExoPlayer with new mediaUri: $mediaUri")
        exoPlayer.setMediaItem(MediaItem.fromUri(mediaUri))
        exoPlayer.prepare()
    }

    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(message.duration?.toLong() ?: 0L) }

    LaunchedEffect(exoPlayer) {
        duration = exoPlayer.duration.takeIf { it > 0 } ?: (message.duration ?: 0L)
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    isPlaying = false
                    currentPosition = 0L
                    exoPlayer.seekTo(0)
                    exoPlayer.pause()
                    Log.d(tag, "Playback ended, reset player")
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                Log.d(tag, "Playback state changed: isPlaying = $playing")
            }
        }

        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = exoPlayer.currentPosition.coerceAtMost(duration)
            delay(200L)

            if (duration == 0L) {
                duration = exoPlayer.duration.takeIf { it > 0 } ?: (message.duration ?: 0L)
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(8.dp)
            .height(60.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                if (isPlaying) {
                    exoPlayer.pause()
                } else {
                    exoPlayer.play()
                }
            }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play"
                )
            }
            Text(
                text = "${currentPosition / 1000}s / ${duration / 1000}s",
                modifier = Modifier.padding(start = 8.dp),
                fontSize = fontSize.sp
            )
        }
        Slider(
            value = if (duration > 0) currentPosition.toFloat() else 0f,
            colors = SliderColors(
                thumbColor = if (isFromMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                activeTrackColor = if (isFromMe) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onBackground,
                activeTickColor = Color.Red,
                inactiveTickColor = Color.Green,
                disabledThumbColor = Color.Gray,
                disabledActiveTrackColor = Color.Black,
                disabledActiveTickColor = Color.Black,
                disabledInactiveTrackColor = Color.Magenta,
                disabledInactiveTickColor = Color.Yellow
            ),
            onValueChange = { newValue ->
                currentPosition = newValue.toLong()
                exoPlayer.seekTo(currentPosition)
            },
            valueRange = 0f..duration.toFloat(),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(top = 4.dp)
        )
    }
}

@Preview
@Composable
fun Prev() {
    AudioMessage(
        message = messageExample, isFromMe = false, fontSize = 16
    )
}