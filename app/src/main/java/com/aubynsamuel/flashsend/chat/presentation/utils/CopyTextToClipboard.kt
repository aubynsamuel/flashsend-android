package com.aubynsamuel.flashsend.chat.presentation.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

fun copyTextToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Copied Text", text)
    clipboard.setPrimaryClip(clip)
}