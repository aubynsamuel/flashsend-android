package com.aubynsamuel.flashsend.chatRoom

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.aubynsamuel.flashsend.R
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit


@Composable
fun AudioRecordingOverlay(
    isRecording: Boolean,
    recordingStartTime: Long,
    resetRecording: () -> Unit,
    sendAudioMessage: () -> Unit,
    modifier: Modifier = Modifier.animateContentSize()
) {
    var playbackTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(key1 = isRecording, key2 = recordingStartTime) {
        while (isRecording) {
            playbackTime = System.currentTimeMillis() - recordingStartTime
            delay(1000)
        }
    }

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier.fillMaxSize()
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.Black.copy(alpha = 0.9f),
            modifier = Modifier
                .padding(bottom = 65.dp)
                .width(300.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                // Recording animation
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(R.raw.recording)
                )

                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(60.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isRecording) "Recording..." else "Recording Complete",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = formatTime(playbackTime),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                AnimatedVisibility(
                    visible = !isRecording,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Discard button
                        Button(
                            onClick = resetRecording,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF5252)
                            ),
                            shape = CircleShape,
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Discard",
                                    tint = Color.White
                                )
//                                Spacer(modifier = Modifier.width(4.dp))
//                                Text("Discard", color = Color.White)
                            }
                        }

                        // Send button
                        Button(
                            onClick = {
                                sendAudioMessage()
                                resetRecording()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            shape = CircleShape,
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = Color.White
                                )
//                                Spacer(modifier = Modifier.width(4.dp))
//                                Text("Send", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatTime(milliseconds: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
            TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%d:%02d", minutes, seconds)
}