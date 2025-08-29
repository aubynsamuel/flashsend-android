package com.aubynsamuel.flashsend.core.presentation.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatMessageTime(date: Date): String {
    val formater = SimpleDateFormat("h:m a", Locale.US)
    return formater.format(date).lowercase()
}