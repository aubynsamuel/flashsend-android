package com.aubynsamuel.flashsend.auth.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.aubynsamuel.flashsend.auth.presentation.viewmodels.AuthViewModel
import com.aubynsamuel.flashsend.core.domain.showToast
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun SetUserDetailsScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var profileUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val storageRef = Firebase.storage.reference
    val coroutineScope = rememberCoroutineScope()
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileUri = uri
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .padding(top = 30.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Let Others Recognize You Easily",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Set Your Username and Profile Picture",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(140.dp))

        // Profile Picture
        Box(modifier = Modifier.clickable { imagePickerLauncher.launch("image/*") }) {
            if (profileUri == null) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(200.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                AsyncImage(
                    model = profileUri, contentDescription = "Selected image",
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(200.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .align(Alignment.Center),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Choose a profile picture",
            modifier = Modifier.clickable { imagePickerLauncher.launch("image/*") })
        Spacer(modifier = Modifier.height(16.dp))

        // Username Input
        BasicTextField(
            value = username,
            onValueChange = { username = it },
            textStyle = TextStyle(),
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
                        Text(
                            text = "Username",
                            color = Color.Black
                        )
                    }
                    innerTextField()
                }
            })


        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        Button(onClick = {
            if (username.isBlank()) {
                showToast(context, "Username cannot be empty")
                return@Button
            }

            isLoading = true
            coroutineScope.launch {
                try {
                    val profileUrl = profileUri?.let { uri ->
                        val imageRef =
                            storageRef.child("profilePictures/${System.currentTimeMillis()}.jpg")
                        imageRef.putFile(uri).await()
                        imageRef.downloadUrl.await().toString()
                    } ?: ""

                    val newData = mapOf(
                        "username" to username,
                        "profileUrl" to profileUrl
                    )

                    authViewModel.updateUserDocument(newData)

                    showToast(context, "Profile updated successfully!")
                    navController.navigate("main?initialPage=0") {
                        popUpTo("setUserDetails") { inclusive = true }
                    }
                } catch (e: Exception) {
                    showToast(context, "Error: ${e.message}", true)
                } finally {
                    isLoading = false
                }
            }
        }) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Save", fontSize = 18.sp)
            }
        }
    }
}
