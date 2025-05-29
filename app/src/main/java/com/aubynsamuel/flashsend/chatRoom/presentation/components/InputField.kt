package com.aubynsamuel.flashsend.chatRoom.presentation.components

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.aubynsamuel.flashsend.chatRoom.presentation.viewmodels.ChatViewModel
import com.aubynsamuel.flashsend.core.data.getCurrentLocation
import com.aubynsamuel.flashsend.core.model.NewUser

@Composable
fun MessageInput(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    onImageClick: () -> Unit,
    isRecording: Boolean,
    onRecordAudio: () -> Unit,
    chatViewModel: ChatViewModel,
    roomId: String,
    userData: NewUser?, recipientToken: String,
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION

    val permissionRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, get the location
            getCurrentLocation(
                context = context,
                onLocationResult = { lat: Double?, lon: Double? ->
                    if (lat != null && lon != null) {
                        chatViewModel.sendLocationMessage(
                            lat,
                            lon,
                            userData?.username ?: "",
                            roomId,
                            userData?.userId ?: "",
                            userData?.profileUrl ?: "",
                            recipientToken
                        )
                    } else {
                        Toast.makeText(
                            context,
                            "Unable to retrieve location",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        } else {
            Toast.makeText(
                context,
                "Location permission denied",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
//    Check if keyboard is shown
//    val density = LocalDensity.current
//    val isKeyboardVisible =
//        WindowInsets.ime.getBottom(density) > 0
    val transition =
        updateTransition(targetState = messageText.isNotBlank(), label = "messageTransition")
    val translateX by transition.animateFloat(
        transitionSpec = { tween(200) }, label = "translationX"
    ) { if (it) 45f else 0f }
    val translate by transition.animateFloat(
        transitionSpec = { tween(200) }, label = "translationX"
    ) { if (it) 80f else 0f }

    val sendIconScale by transition.animateFloat(
        transitionSpec = { tween(100) }, label = "sendIconScale"
    ) { if (it) 0.3f else 1f }

    val placeIconScale by transition.animateFloat(
        transitionSpec = { tween(100, delayMillis = 100) }, label = "placeIconScale"
    ) { if (it) 1f else 0.3f }

    val homeIconAlpha by transition.animateFloat(
        transitionSpec = { tween(150) }, label = "homeIconAlpha"
    ) { if (it) 0f else 1f }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 3.dp)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BasicTextField(
            value = messageText,
            onValueChange = onMessageChange,
            maxLines = 5,
            singleLine = false,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
            ),
            modifier = Modifier.weight(1f),
            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onPrimaryContainer),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(25.dp)
                        )
                        .fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 15.dp)
                    ) {
                        Row(modifier = Modifier.weight(1f)) {
                            Box {
                                innerTextField()
                                if (messageText.isBlank()) Text("Type a message")
                            }
                        }
                        Row(
                            modifier = Modifier,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddLocationAlt,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .graphicsLayer {
                                        translationX = translate
                                    }
                                    .clickable(onClick = { showDialog = true })
                            )
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Add Photo",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .graphicsLayer {
                                        translationX = translateX
                                        alpha = homeIconAlpha
                                    }
                                    .clickable(onClick = { onImageClick() })
                            )
                        }
                    }
                }
            }
        )

        IconButton(
            onClick = if (messageText.isNotBlank()) onSend else onRecordAudio,
            modifier = Modifier
                .background(
                    color = if (!isRecording) MaterialTheme.colorScheme.primaryContainer else Color.Red,
                    shape = CircleShape
                )
        ) {
            AnimatedVisibility(visible = messageText.isBlank()) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Mic",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.graphicsLayer {
                        scaleX = sendIconScale
                        scaleY = sendIconScale
                    })
            }

            AnimatedVisibility(visible = messageText.isNotBlank()) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.graphicsLayer {
                        scaleX = placeIconScale
                        scaleY = placeIconScale
                    })
            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Share Location") },
            text = { Text("You are about to share your location, do you want to continue?") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false

                    // Check if the permission is already granted
                    if (ContextCompat.checkSelfPermission(
                            context,
                            locationPermission
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Permission is granted, get location immediately
                        getCurrentLocation(context) { lat, lon ->
                            if (lat != null && lon != null) {
                                chatViewModel.sendLocationMessage(
                                    lat,
                                    lon,
                                    userData?.username ?: "",
                                    roomId,
                                    userData?.userId ?: "",
                                    userData?.profileUrl ?: "",
                                    recipientToken
                                )
                            } else {
                                Toast.makeText(
                                    context,
                                    "Unable to retrieve location",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        // Permission not granted, request it.
                        permissionRequest.launch(locationPermission)
                    }
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}


//@Preview
//@Composable
//fun PrevInputToolBar() {
//    MessageInput(messageText = "", onMessageChange = {}, onSend = {},
//        onImageClick = {},
//        isRecording = false,
//        onRecordAudio = {},
//        onLocationRetrieved = {}
//    )
//}