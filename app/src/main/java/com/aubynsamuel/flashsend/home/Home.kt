package com.aubynsamuel.flashsend.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.aubynsamuel.flashsend.auth.AuthViewModel
import com.google.firebase.Timestamp

data class RoomData(
    val roomId: String,
    val lastMessage: String?,
    val lastMessageTimestamp: Timestamp?,
    val lastMessageSenderId: String?,
    val otherParticipant: User
)

data class User(
    val userId: String,
    val username: String,
    val profileUrl: String,
    val otherUsersDeviceToken: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController, authViewModel: AuthViewModel
) {
    val homeViewModel: HomeViewModel = viewModel()
    val rooms by homeViewModel.rooms.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (!authState) {
            navController.navigate("auth") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    Scaffold(topBar = {
        Row(
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(top = 15.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Flash Send",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
            Row {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clickable(onClick = { navController.navigate("searchUsers") })
                        .padding(end = 5.dp)
                )
                Icon(
                    Icons.Outlined.MoreVert,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clickable(onClick = { authViewModel.logout() })
                        .padding(horizontal = 5.dp)
                )
            }
        }
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = { navController.navigate("searchUsers") },
            modifier = Modifier.padding(bottom = 50.dp, end = 10.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Chat")
        }
    }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp)
                ) {
                    items(rooms) { room ->
                        ChatListItem(room, navController)
                    }
                }
            }
        }
    }
}