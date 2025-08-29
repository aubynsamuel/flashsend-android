package com.aubynsamuel.flashsend.chatRoom.presentation.utils

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun createFile(context: Context): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    return File.createTempFile("JPEG_${timestamp}_", ".jpg", context.cacheDir)
}