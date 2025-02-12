package com.aubynsamuel.flashsend.functions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast

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