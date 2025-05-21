package com.aubynsamuel.flashsend

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FlashSendApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}