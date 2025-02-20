package com.aubynsamuel.flashsend

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aubynsamuel.flashsend.functions.logger
import com.aubynsamuel.flashsend.settings.SettingsRepository
import com.aubynsamuel.flashsend.settings.SettingsViewModel
import com.aubynsamuel.flashsend.settings.SettingsViewModelFactory
import com.aubynsamuel.flashsend.settings.dataStore
import com.aubynsamuel.flashsend.ui.theme.FlashSendTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        createNotificationChannel()
        enableEdgeToEdge()
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                logger("FCMToken", token)
            }
        }
        setContent {
            // Instantiate your DataStore and repository from the application context
            val context = LocalContext.current
            val dataStore = context.applicationContext.dataStore
            val settingsRepository = remember { SettingsRepository(dataStore) }
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(settingsRepository)
            )
            // Collect settings state to extract the theme mode
            val settingsState by settingsViewModel.uiState.collectAsState()

            // Pass the themeMode to FlashSendTheme
            FlashSendTheme(themeMode = settingsState.themeMode) {
                ChatAppNavigation()
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val bottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            view.updatePadding(bottom = bottom)
            insets
        }
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Regular Notifications"
            val descriptionText = "Channel for notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "flash"
        const val KEY_TEXT_REPLY = "key_text_reply"
    }
}