package com.aubynsamuel.flashsend.chatRoom.messageTypes

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aubynsamuel.flashsend.R
import com.aubynsamuel.flashsend.functions.ChatMessage

@Composable
fun LocationMessage(message: ChatMessage) {
    val location = message.location ?: return
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .height(120.dp)
            .clickable {
                val uri = Uri.parse("geo:${location.latitude},${location.longitude}")
                Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.google.android.apps.maps")
                    context.startActivity(this)
                }
            }
    ) {
        AsyncImage(
            model = R.drawable.map,
            contentDescription = "Location preview",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}