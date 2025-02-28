package com.aubynsamuel.flashsend.chatRoom

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material3.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.aubynsamuel.flashsend.auth.CurrentUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ImagePreviewScreen(
    imageUri: Uri,
    chatViewModel: ChatViewModel = viewModel(),
    navController: NavController,
    roomId: String,
    takenFromCamera: String?,
    profileUrl: String,
    recipientsToken: String
) {
    var caption by remember { mutableStateOf("") }
    val context = LocalContext.current
    var selectedPicture by remember { mutableStateOf<Uri?>(imageUri) }
    var croppedPicture by remember { mutableStateOf<Uri?>(selectedPicture) }

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
        if (takenFromCamera == "1") {
            navController.popBackStack()
            navController.popBackStack()
        } else navController.popBackStack()
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
                    CircularProgressIndicator(
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
                                        Toast.makeText(
                                            context, "Failed to upload image", Toast.LENGTH_SHORT
                                        ).show()
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
