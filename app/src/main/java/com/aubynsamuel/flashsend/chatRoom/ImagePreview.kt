package com.aubynsamuel.flashsend.chatRoom

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.aubynsamuel.flashsend.R
import com.aubynsamuel.flashsend.auth.AuthViewModel
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun ImagePreviewScreen(
    imageUri: Uri,
    chatViewModel: ChatViewModel = viewModel(),
    navController: NavController,
    authViewModel: AuthViewModel,
    roomId: String,
    takenFromCamera: String?,
    profileUrl: String,
    recipientsToken: String
) {
    var caption by remember { mutableStateOf("") }
    val context = LocalContext.current
    var selectedPicture by remember { mutableStateOf<Uri?>(imageUri) }
    var croppedPicture by remember { mutableStateOf<Uri?>(selectedPicture) }
    val userData by authViewModel.userData.collectAsState()

    // Launcher for cropping the image using our custom contract.
    val cropImageLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { croppedUri: Uri? ->
        croppedUri?.let { croppedPicture = it }
    }

    // Launcher for picking an image; it simply updates selectedPicture.
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedPicture = it
            croppedPicture = it
        }
    }

    fun onPickAnother() {
        imagePickerLauncher.launch("image/*")
    }

    // Trigger the crop action when the user taps the crop icon.
    fun onCrop() {
        selectedPicture?.let { uri ->
            cropImageLauncher.launch(uri)
        } ?: Toast.makeText(context, "Please select an image first", Toast.LENGTH_SHORT).show()
    }

    fun onCancel() {
        navController.popBackStack()
    }

    fun onSend(imageUrl: String) {
        chatViewModel.sendImageMessage(
            caption = caption,
            imageUrl = imageUrl,
            senderName = userData?.username ?: "",
            roomId = roomId,
            currentUserId = userData?.userId ?: "",
            profileUrl = profileUrl,
            recipientsToken = recipientsToken
        )
        navController.popBackStack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 10.dp),
            horizontalArrangement = if (takenFromCamera == "0") Arrangement.SpaceBetween else Arrangement.End
        ) {
            if (takenFromCamera == "0") {
                Button(onClick = { onPickAnother() }) { Text("Pick Another") }
            }
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cancel",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .clickable(onClick = { onCancel() })
                    .align(Alignment.CenterVertically)
                    .size(30.dp)
            )
        }

        // Display the selected image.
        croppedPicture?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(model = uri),
                contentDescription = "Selected Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
            )
        }

        // Crop icon to launch the uCrop editor.
        Icon(
            imageVector = Icons.Default.Crop,
            contentDescription = "Crop Image",
            modifier = Modifier
                .padding(8.dp)
                .size(35.dp)
                .clickable { onCrop() }, tint = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            TextField(
                value = caption,
                onValueChange = { caption = it },
                label = { Text("Add a caption") },
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Default.Send,
                contentDescription = "Send Image Button",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(40.dp)
                    .clickable(onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            croppedPicture?.let { uri ->
                                val imageUrl =
                                    chatViewModel.uploadImage(uri, userData?.username ?: "")
                                withContext(Dispatchers.Main) {
                                    if (imageUrl != null) {
                                        onSend(imageUrl)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Failed to upload image",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    })
            )
        }
    }
}

// Custom ActivityResultContract for cropping images with uCrop.
class CropImageContract : ActivityResultContract<Uri, Uri?>() {
    override fun createIntent(context: Context, input: Uri): Intent {
        // Destination URI for the cropped image.
        val destinationUri = Uri.fromFile(
            File(context.cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
        )
        // Set up uCrop options to enable editing features.
        val options = UCrop.Options().apply {
            setToolbarTitle("Edit Image")
            setFreeStyleCropEnabled(true) // Allow free style cropping.
            // Add any additional uCrop configuration here.
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
            .withAspectRatio(15f, 10f)
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
