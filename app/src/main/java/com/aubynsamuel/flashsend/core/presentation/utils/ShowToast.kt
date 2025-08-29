package com.aubynsamuel.flashsend.core.presentation.utils

import android.content.Context
import android.widget.Toast

fun showToast(context: Context, message: String, long: Boolean = false) {
    Toast.makeText(
        context, message, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    ).show()
}