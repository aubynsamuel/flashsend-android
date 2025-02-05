package com.aubynsamuel.flashsend

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aubynsamuel.flashsend.chatRoom.ChatScreen
import com.aubynsamuel.flashsend.home.HomeViewModel
import com.aubynsamuel.flashsend.ui.theme.FlashSendTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            FlashSendTheme {
                val homeViewModel: HomeViewModel = viewModel()
                ChatScreen()
//                AuthScreen()
//                HomeScreen (homeViewModel, {})

            }
        }
    }
}
