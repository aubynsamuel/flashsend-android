package com.aubynsamuel.flashsend.chatRoom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.aubynsamuel.flashsend.R
import com.aubynsamuel.flashsend.chatRoom.messageTypes.FullScreenImageViewer
import com.aubynsamuel.flashsend.functions.User
import java.net.URLDecoder


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherUserProfile(
    navController: NavController,
    userData: User
) {
    var isExpanded by remember { mutableStateOf(false) }
    val decodedUsername = URLDecoder.decode(userData.username, "UTF-8")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back Button",
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .clickable(onClick = { navController.popBackStack() })
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Picture
                    AsyncImage(
                        model = userData.profileUrl,
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .clickable(onClick = { isExpanded = true }),
                        contentScale = ContentScale.Crop,
                        error = rememberAsyncImagePainter(R.drawable.ic_launcher_foreground)
                    )

                    if (isExpanded) {
                        FullScreenImageViewer(userData.profileUrl.toString()) {
                            isExpanded = false
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = decodedUsername,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

//                    Text(
//                        text = userData?.email ?: "Email",
//                        style = MaterialTheme.typography.bodyLarge,
//                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
//                    )
                }
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
                        value = decodedUsername
                    )

//                    ProfileDetailItem(
//                        icon = Icons.Default.Email,
//                        label = "Email",
//                        value = userData?.email ?: "Not set"
//                    )
                }
            }

            // Action Buttons
            Spacer(modifier = Modifier.height(24.dp))

//            FilledTonalButton(
//                onClick = { navController.navigate("editProfile") },
//                modifier = Modifier.fillMaxWidth(0.8f)
//            ) {
//                Icon(Icons.Default.Edit, contentDescription = null)
//                Spacer(modifier = Modifier.width(8.dp))
//                Text("Edit Profile")
//            }

            Spacer(modifier = Modifier.height(8.dp))
            Card(
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Default.Message,
                    contentDescription = "",
                    modifier = Modifier
                        .size(70.dp)
                        .clickable(onClick = { navController.popBackStack() })
                        .padding(10.dp)
                )
            }
            Text(
                text = "Message",
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 5.dp)
            )

//            Button(
//                onClick = {
//                    authViewModel.logout()
//                    navController.navigate("auth") {
//                        popUpTo(0)
//                    }
//                },
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = MaterialTheme.colorScheme.errorContainer,
//                    contentColor = MaterialTheme.colorScheme.onErrorContainer
//                ),
//                modifier = Modifier.fillMaxWidth(0.8f)
//            ) {
//                Icon(Icons.AutoMirrored.Default.Logout, contentDescription = null)
//                Spacer(modifier = Modifier.width(8.dp))
//                Text("Log Out")
//            }
        }
    }

//    LaunchedEffect(Unit) {
//        authViewModel.loadUserData()
//    }
}

@Composable
fun ProfileDetailItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}