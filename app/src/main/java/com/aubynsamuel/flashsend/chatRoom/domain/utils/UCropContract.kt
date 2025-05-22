package com.aubynsamuel.flashsend.chatRoom.domain.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.ContextCompat
import com.aubynsamuel.flashsend.R
import com.yalantis.ucrop.UCrop
import java.io.File

class CropImageContract : ActivityResultContract<Uri, Uri?>() {
    override fun createIntent(context: Context, input: Uri): Intent {

        val destinationUri = Uri.fromFile(
            File(context.cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
        )

        val options = UCrop.Options().apply {
            setToolbarTitle("Edit Image")
            setFreeStyleCropEnabled(true) // Allow free style cropping.
            setToolbarColor(ContextCompat.getColor(context, R.color.toolbar_color))
            setStatusBarColor(ContextCompat.getColor(context, R.color.status_bar_color))
            setToolbarWidgetColor(ContextCompat.getColor(context, R.color.toolbar_widget_color))
            setActiveControlsWidgetColor(
                ContextCompat.getColor(
                    context,
                    R.color.active_widget_color
                )
            )
        }
        return UCrop.of(input, destinationUri)
            .withOptions(options)
            .withAspectRatio(12f, 16f)
            .withMaxResultSize(1080, 1080)
            .getIntent(context)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (resultCode == Activity.RESULT_OK && intent != null) {
            UCrop.getOutput(intent)
        } else {
            null
        }
    }
}
