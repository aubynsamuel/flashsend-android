package com.aubynsamuel.flashsend.notifications.presentation

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.aubynsamuel.flashsend.notifications.domain.showNotification

@Composable
fun NotificationTestScreen(context: Context) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val messages = listOf<String>(
            "Hello Junior", "No bs today", "I don't like it",
            "Alright reply to this one"
        )
        Text(
            text = "Show a notification",
            modifier = Modifier.clickable(onClick = {
                showNotification(
                    context,
                    message = messages.random(),
                    sender = "Samuel",
                    id = "as",
                    sendersUserId = "",
                    recipientsUserId = ""
                )
            }), color = MaterialTheme.colorScheme.onBackground
        )
    }
}