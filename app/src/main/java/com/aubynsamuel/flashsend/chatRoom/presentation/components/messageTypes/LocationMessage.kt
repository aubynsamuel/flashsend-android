package com.aubynsamuel.flashsend.chatRoom.presentation.components.messageTypes

import android.content.Intent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.aubynsamuel.flashsend.R
import com.aubynsamuel.flashsend.chatRoom.domain.utils.vibrateDevice
import com.aubynsamuel.flashsend.core.model.ChatMessage

@Composable
fun LocationMessage(message: ChatMessage, showPopUp: () -> Unit) {
    val location = message.location ?: return
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .height(120.dp)
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = {
                    vibrateDevice(context)
                    showPopUp()
                }, onTap = {
                    val uri = "geo:${location.latitude},${location.longitude}".toUri()
                    Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage("com.google.android.apps.maps")
                        context.startActivity(this)
                    }
                })
            },
    ) {
        AsyncImage(
            model = R.drawable.map,
            contentDescription = "Location preview",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}