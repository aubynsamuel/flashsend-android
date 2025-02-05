package com.aubynsamuel.flashsend.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

data class RoomData(
    val roomId: String,
    val lastMessage: String?,
    val lastMessageTimestamp: Long?,
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
    viewModel: HomeViewModel = viewModel(),  // Specify the type explicitly
    onNavigateToSearch: () -> Unit
) {
    val rooms by viewModel.rooms.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Flash Send") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToSearch) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Chat")
            }
        }
    ) { paddingValues ->
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
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(rooms) { room ->
                        ChatListItem(room)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatListItem(room: RoomData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { /* Navigate to chat */ },
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = room.otherParticipant.username,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            room.lastMessage?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}
