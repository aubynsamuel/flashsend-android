package com.aubynsamuel.flashsend.chat.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material3.Button
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.aubynsamuel.flashsend.chat.presentation.utils.CropImageContract
import com.aubynsamuel.flashsend.chat.presentation.viewmodels.ChatViewModel
import com.aubynsamuel.flashsend.core.data.CurrentUser
import com.aubynsamuel.flashsend.core.presentation.utils.showToast
import com.aubynsamuel.flashsend.navigation.safePopBackStack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ImagePreviewScreen(
    imageUri: Uri,
    chatViewModel: ChatViewModel,
    navController: NavController,
    takenFromCamera: String?,
    recipientsToken: String,
) {
    var caption by remember { mutableStateOf("") }
    val context = LocalContext.current
    var selectedPicture by remember { mutableStateOf<Uri?>(imageUri) }
    var croppedPicture by remember { mutableStateOf(selectedPicture) }

    var loading by remember { mutableStateOf(false) }

    val userData by CurrentUser.userData.collectAsStateWithLifecycle()

    val cropImageLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { croppedUri: Uri? ->
        croppedUri?.let { croppedPicture = it }
    }

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

    fun onCrop() {
        selectedPicture?.let { uri ->
            cropImageLauncher.launch(uri)
        } ?: showToast(context, "Please select an image first")
    }

    fun onCancel() {
        navController.safePopBackStack()
    }

    fun onSend(imageUrl: String) {
        chatViewModel.sendImageMessage(
            caption = caption,
            imageUrl = imageUrl,
            recipientsToken = recipientsToken
        )
        if (takenFromCamera == "1") {
            navController.safePopBackStack()
            navController.safePopBackStack()
        } else navController.safePopBackStack()
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

        croppedPicture?.let { uri ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = uri),
                    contentDescription = "Selected Image",
                    modifier = Modifier.fillMaxSize()
                )
                if (loading) {
                    CircularWavyProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }


        Icon(
            imageVector = Icons.Default.Crop,
            contentDescription = "Crop Image",
            modifier = Modifier
                .size(35.dp)
                .clickable { onCrop() },
            tint = MaterialTheme.colorScheme.onBackground
        )

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
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                )
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
                                loading = true
                                val imageUrl =
                                    chatViewModel.uploadImage(uri, userData?.username ?: "")
                                withContext(Dispatchers.Main) {
                                    if (imageUrl != null) {
                                        onSend(imageUrl)
                                    } else {
                                        showToast(context, "Failed to upload image")
                                    }
                                    loading
                                }
                            }
                        }
                    })
            )
        }
    }
}
