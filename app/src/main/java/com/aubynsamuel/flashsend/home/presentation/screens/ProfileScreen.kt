package com.aubynsamuel.flashsend.home.presentation.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.aubynsamuel.flashsend.R
import com.aubynsamuel.flashsend.core.data.CurrentUser
import com.aubynsamuel.flashsend.core.presentation.navigation.EditProfileDC
import com.aubynsamuel.flashsend.core.presentation.navigation.FullScreenImageViewerDC
import com.aubynsamuel.flashsend.home.presentation.components.ProfileDetailItem
import com.aubynsamuel.flashsend.navigation.LocalSharedTransitionScope


@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    animatedScope: AnimatedVisibilityScope,

    ) {
    val userData by CurrentUser.userData.collectAsStateWithLifecycle()
    val sharedTransitionScope = LocalSharedTransitionScope.current

    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = { Text("My Profile", fontWeight = FontWeight.Medium) },
            colors = TopAppBarDefaults.topAppBarColors().copy(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary
            ),
        )
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Picture
                with(sharedTransitionScope) {
                    val imageUri = userData?.profileUrl
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop,
                        error = rememberAsyncImagePainter(R.drawable.person),
                        modifier = Modifier
                            .clickable(onClick = {
                                navController.navigate(
                                    FullScreenImageViewerDC(
                                        imageUri = imageUri ?: "",
                                    )
                                )
                            })
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "image/${imageUri}"),
                                animatedVisibilityScope = animatedScope
                            )
                            .clip(CircleShape)
                            .size(120.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = userData?.username ?: "Username",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = userData?.email ?: "Email",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }

            // Profile Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ProfileDetailItem(
                        icon = Icons.Default.Person,
                        label = "Username",
                        value = userData?.username ?: "Not set"
                    )

                    ProfileDetailItem(
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = userData?.email ?: "Not set"
                    )
                }
            }

            // Action Buttons
            Spacer(modifier = Modifier.height(24.dp))

            FilledTonalButton(
                onClick = { navController.navigate(EditProfileDC) },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile")
            }
        }
    }
}