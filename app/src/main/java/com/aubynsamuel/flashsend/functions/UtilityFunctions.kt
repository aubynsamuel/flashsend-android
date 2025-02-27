package com.aubynsamuel.flashsend.functions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun logger(tag: String, message: String) {
    Log.d(tag, message)
}

fun copyTextToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Copied Text", text)
    clipboard.setPrimaryClip(clip)
}

fun showToast(context: Context, message: String, long: Boolean = false) {
    Toast.makeText(
        context, message, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    ).show()
}

fun createRoomId(userId: String, currentUserId: String): String {
    val ids = listOf<String>(userId, currentUserId)
    return ids.sorted().joinToString("_")
}

fun createFile(context: Context): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    return File.createTempFile("JPEG_${timestamp}_", ".jpg", context.cacheDir)
}

fun formatMessageTime(date: Date): String {
    val formater = SimpleDateFormat("h:m a", Locale.US)
    return formater.format(date).lowercase()
}