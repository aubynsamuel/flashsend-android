package com.aubynsamuel.flashsend.chatRoom

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.Text
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.aubynsamuel.flashsend.auth.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ImagePreviewScreen(
    imageUri: Uri,
    chatViewModel: ChatViewModel = viewModel(),
    navController: NavController,
    authViewModel: AuthViewModel,
    roomId: String
) {
    var caption by remember { mutableStateOf("") }
    val context = LocalContext.current
    var selectedPicture by remember { mutableStateOf(imageUri) }
    val userData by authViewModel.userData.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedPicture = uri
        }
    }

    fun onPickAnother() {
        imagePickerLauncher.launch("image/*")
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
            currentUserId = userData?.userId ?: ""
        )
        navController.popBackStack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(vertical = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween)
        {
            Button(onClick = { onPickAnother() }) { Text("Pick Another") }
            Icon(
                Icons.Default.Close,
                contentDescription = "Cancel",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .clickable(onClick = { onCancel() })
                    .align(Alignment.CenterVertically)
                    .size(30.dp)
            )
        }

        // Display the selected image.
        Image(
            painter = rememberAsyncImagePainter(model = selectedPicture),
            contentDescription = "Selected Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
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
                shape = RoundedCornerShape(24.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                Icons.AutoMirrored.Default.Send,
                contentDescription = "Send Image Button",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .size(40.dp)
                    .clickable(onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            val imageUrl =
                                chatViewModel.uploadImage(selectedPicture, userData?.username ?: "")
                            withContext(Dispatchers.Main) {
                                if (imageUrl != null) {
                                    onSend(imageUrl)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Failed to upload image",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            }
                        }
                    })
            )
        }
    }
}