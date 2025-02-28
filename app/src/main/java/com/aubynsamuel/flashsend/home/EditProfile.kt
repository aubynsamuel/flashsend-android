package com.aubynsamuel.flashsend.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.aubynsamuel.flashsend.R
import com.aubynsamuel.flashsend.auth.AuthViewModel
import com.aubynsamuel.flashsend.auth.CurrentUser
import com.aubynsamuel.flashsend.chatRoom.CropImageContract
import com.aubynsamuel.flashsend.functions.showToast
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun EditProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
) {
    val userData by CurrentUser.userData.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var username by remember { mutableStateOf(userData?.username ?: "") }
    var profileUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val storageRef = Firebase.storage.reference
    val coroutineScope = rememberCoroutineScope()

    val cropImageLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { croppedUri: Uri? ->
        croppedUri?.let { profileUri = it }
    }
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { nonNullUri ->
            profileUri = nonNullUri
            cropImageLauncher.launch(nonNullUri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .padding(top = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.AutoMirrored.Default.ArrowBack,
            contentDescription = "",
            modifier = Modifier
                .align(Alignment.Start)
                .clickable(onClick = { navController.popBackStack() }),
            tint = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Edit Profile",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Update your username and/or profile picture",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(70.dp))

        // Profile Picture
        Box(modifier = Modifier.clickable { imagePickerLauncher.launch("image/*") }) {
            AsyncImage(
                model = if (profileUri != null) profileUri else userData?.profileUrl,
                contentDescription = "Selected image",
                modifier = Modifier
                    .clip(CircleShape)
                    .size(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .align(Alignment.Center),
                contentScale = ContentScale.Crop,
                error = rememberAsyncImagePainter(R.drawable.person)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Choose a profile picture",
            modifier = Modifier.clickable { imagePickerLauncher.launch("image/*") },
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Username Input
        BasicTextField(value = username,
            onValueChange = { username = it },
            textStyle = TextStyle(fontSize = 16.sp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Sentences
            ),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray, shape = MaterialTheme.shapes.medium)
                .padding(12.dp),
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .padding(horizontal = 10.dp)
                ) {
                    if (username.isEmpty()) {
                        Text(text = "Username", color = Color.Gray)
                    }
                    innerTextField()
                }
            })

        Spacer(modifier = Modifier.height(20.dp))

        // Save Button
        Button(onClick = {
            if (username.isBlank() && profileUri == null) {
                showToast(context, "Please update at least one field")
                return@Button
            }
            isLoading = true
            coroutineScope.launch {
                try {
                    val newData = mutableMapOf<String, Any>()

                    if (username.isNotBlank()) {
                        newData["username"] = username
                    }

                    // Upload new profile picture if selected.
                    if (profileUri != null) {
                        val imageRef =
                            storageRef.child("profilePictures/${System.currentTimeMillis()}.jpg")
                        imageRef.putFile(profileUri!!).await()
                        val profileUrl = imageRef.downloadUrl.await().toString()
                        newData["profileUrl"] = profileUrl
                    }

                    // Update Firestore document if there is at least one field to update.
                    if (newData.isNotEmpty()) {
                        authViewModel.updateUserDocument(newData)
                        showToast(context, "Profile updated successfully!")
                        authViewModel.loadUserData()
                    } else {
                        showToast(context, "No updates provided")
                    }
                } catch (e: Exception) {
                    showToast(context, "Error: ${e.message}", true)
                } finally {
                    isLoading = false
                }
            }
        }) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Save", fontSize = 18.sp)
            }
        }
    }
}
