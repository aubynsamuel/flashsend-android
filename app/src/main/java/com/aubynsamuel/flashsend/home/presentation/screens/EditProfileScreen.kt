package com.aubynsamuel.flashsend.home.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.aubynsamuel.flashsend.R
import com.aubynsamuel.flashsend.auth.presentation.viewmodels.AuthViewModel
import com.aubynsamuel.flashsend.chat.presentation.utils.CropImageContract
import com.aubynsamuel.flashsend.core.data.CurrentUser
import com.aubynsamuel.flashsend.core.presentation.utils.showToast
import com.aubynsamuel.flashsend.navigation.LocalSharedTransitionScope
import com.aubynsamuel.flashsend.navigation.safePopBackStack
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.abs


@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    animatedScope: AnimatedContentScope,
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val density = LocalDensity.current
    val maxHeight = 220.dp
    val minHeight = 120.dp
    val maxHeightPx = with(density) { maxHeight.toPx() }
    val minHeightPx = with(density) { minHeight.toPx() }
    val headerHeight = remember { Animatable(maxHeightPx) }
    val headerHeightDp = with(density) { headerHeight.value.toDp() }

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

    fun updateProfile() {
        if (username.isBlank() && profileUri == null) {
            showToast(context, "Please update at least one field")
            return
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
                        storageRef.child("profilePictures/${username}_profile_pic.jpg")
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
    }

    val exitUntilCollapsedScrollBehavior = remember {
        object : NestedScrollConnection {
            private val snapThreshold = (maxHeightPx + minHeightPx) / 2
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < 0f) { // Scroll Up (Collapse)
                    val previousHeight = headerHeight.value
                    // Calculate the new height, constrained by min/max
                    val newHeaderHeight =
                        (headerHeight.value + delta).coerceIn(minHeightPx, maxHeightPx)

                    // Consumed is the actual change in header height (must be negative)
                    val consumed = newHeaderHeight - previousHeight

                    if (abs(consumed) > 0.5f) { // Check to prevent tiny floats from consuming
                        coroutineScope.launch {
                            headerHeight.animateTo(newHeaderHeight)
                        }
                        return Offset(0f, consumed)
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                val delta = available.y
                if (delta > 0f) { // Scroll Down (Expand)
                    val previousHeight = headerHeight.value
                    // Calculate the new height, constrained by min/max
                    val newHeaderHeight =
                        (headerHeight.value + delta).coerceIn(minHeightPx, maxHeightPx)

                    // Consumed is the actual change in header height (must be positive)
                    val consumed = newHeaderHeight - previousHeight

                    if (abs(consumed) > 0.5f) {
                        coroutineScope.launch {
                            headerHeight.animateTo(newHeaderHeight)
                        }
                        return Offset(0f, consumed)
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity,
            ): Velocity {
                val currentHeight = headerHeight.value
                val targetHeight = if (currentHeight < snapThreshold) {
                    minHeightPx
                } else {
                    maxHeightPx
                }
                if (currentHeight != targetHeight) {
                    headerHeight.animateTo(targetHeight)
                }
                return available
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(exitUntilCollapsedScrollBehavior),
        topBar = {
            TopAppBar(
                title = {
                    with(sharedTransitionScope) {
                        Text(
                            "Edit Profile",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "text/editProfileScreenTitle"),
                                animatedVisibilityScope = animatedScope
                            )
                        )
                    }
                },
                actions = {
                    with(sharedTransitionScope) {
                        IconButton(
                            onClick = { updateProfile() },
                            modifier = Modifier.sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "icon/editProfileToProfile"),
                                animatedVisibilityScope = animatedScope
                            )
                        ) {
                            Icon(Icons.Default.Update, contentDescription = "Edit Profile")
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.safePopBackStack() },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go to Profile Screen"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .height(headerHeightDp)
            ) {
                with(sharedTransitionScope) {
                    val imageUri = userData?.profileUrl
                    AsyncImage(
                        model = if (profileUri != null) profileUri else userData?.profileUrl,
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop,
                        error = rememberAsyncImagePainter(R.drawable.person),
                        modifier = Modifier
                            .clickable(onClick = {
                                imagePickerLauncher.launch("image/*")
                            })
                            .border(
                                2.dp,
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                            .clip(CircleShape)
                            .size(headerHeightDp - 80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Change profile picture",
                    modifier = Modifier.clickable { imagePickerLauncher.launch("image/*") },
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            // Details
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text("Info", textAlign = TextAlign.Left, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(10.dp))

                // Username TextField
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Username",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        OutlinedTextField(
                            value = username.ifEmpty { "Username" },
                            onValueChange = { username = it },
                            label = { Text("Username") },
                            shape = RoundedCornerShape(10.dp),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Done
                            ),
                        )
                    }
                }
            }
        }
    }
}