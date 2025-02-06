package com.aubynsamuel.flashsend.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.aubynsamuel.flashsend.R
import com.aubynsamuel.flashsend.Screen
import com.aubynsamuel.flashsend.auth.AuthViewModel
import java.net.URLEncoder

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
    navController: NavController,
    homeViewModel: HomeViewModel,
    authViewModel: AuthViewModel
) {
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
                .background(Color.DarkGray)
                .padding(top = 15.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Flash Send",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
            Icon(
                Icons.Outlined.MoreVert,
                contentDescription = "",
                tint = Color.White,
                modifier = Modifier
                    .clickable(onClick = { authViewModel.logout() })
            )
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

@Composable
fun ChatListItem(room: RoomData, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val user = room.otherParticipant
                val encodedUsername = URLEncoder.encode(user.username, "UTF-8")
                val encodedProfileUrl = URLEncoder.encode(user.profileUrl, "UTF-8")
                val roomId = room.roomId
                navController.navigate(
                    Screen.ChatRoom.createRoute(
                        username = encodedUsername,
                        userId = user.userId,
                        deviceToken = user.otherUsersDeviceToken,
                        profileUrl = encodedProfileUrl,
                        roomId = roomId
                    )
                )
            }, shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 0.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (room.otherParticipant.profileUrl.isNotEmpty()) {
                AsyncImage(
                    model = room.otherParticipant.profileUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(45.dp), contentScale = ContentScale.FillBounds
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(45.dp)
                )
            }
            Column {
                Text(
                    text = room.otherParticipant.username,
                    modifier = Modifier.padding(start = 5.dp),
                    color = Color.Gray,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                room.lastMessage?.let {
                    Text(
                        text = it, color = Color.Gray, modifier = Modifier.padding(start = 7.dp),
                        fontSize = 15.sp,
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Column {
                Text(
                    text = room.lastMessageTimestamp.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 15.sp,
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${8}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color.Red
                    )
                }

            }

        }
    }
}

@Preview
@Composable
fun PrevChatItem() {
    ChatListItem(
        item,
        navController = rememberNavController()
    )
}

val item = RoomData(
    roomId = "room_1",
    lastMessage = "Hey, how's it going?",
    lastMessageTimestamp = System.currentTimeMillis() - 60000,
    lastMessageSenderId = "user_1",
    otherParticipant = User(
        userId = "user_1",
        username = "Alice Johnson",
        profileUrl = "",
        otherUsersDeviceToken = "token_1"
    )
)