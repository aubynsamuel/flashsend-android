package com.aubynsamuel.flashsend

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.aubynsamuel.flashsend.chatRoom.ChatScreen
import com.aubynsamuel.flashsend.ui.theme.FlashSendTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlashSendTheme {
                ChatScreen()
            }
        }
    }
}
