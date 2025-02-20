package com.aubynsamuel.flashsend.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.aubynsamuel.flashsend.Screen
import com.aubynsamuel.flashsend.chatRoom.ChatViewModel
import com.aubynsamuel.flashsend.chatRoom.formatMessageTime
import com.aubynsamuel.flashsend.chatRoom.messageTypes.FullScreenImageViewer
import com.aubynsamuel.flashsend.functions.RoomData
import com.aubynsamuel.flashsend.functions.logger
import com.aubynsamuel.flashsend.functions.showToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URLEncoder

@Composable
fun ChatListItem(room: RoomData, navController: NavController, chatViewModel: ChatViewModel) {
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    var unreadCount by remember { mutableIntStateOf(0) }
    var isExpanded by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: return

    fun getUnreadMessages(roomId: String, otherUserId: String) {
        firestore.collection("rooms").document(roomId).collection("messages")
            .where(Filter.equalTo("read", false)).where(Filter.equalTo("senderId", otherUserId))
            .addSnapshotListener { snapShot, error ->
                if (error != null) {
                    logger("chatPack", error.message.toString())
                    return@addSnapshotListener
                }
                snapShot?.let {
                    unreadCount = it.documents.size
                }
            }
    }

    LaunchedEffect(unreadCount) {
        chatViewModel.prefetchNewMessagesForRoom(roomId = room.roomId)
    }

    getUnreadMessages(room.roomId, room.otherParticipant.userId)

//    Chat list item card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val user = room.otherParticipant
                val encodedUsername = URLEncoder.encode(user.username, "UTF-8")
                val encodedProfileUrl = URLEncoder.encode(user.profileUrl, "UTF-8")
                navController.navigate(
                    Screen.ChatRoom.createRoute(
                        username = encodedUsername,
                        userId = user.userId,
                        deviceToken = user.deviceToken,
                        profileUrl = encodedProfileUrl,
                    )
                )
            },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 10.dp)
                .padding(end = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(0.85f),
            ) {
//                profile pic, username and last message
                if (room.otherParticipant.profileUrl.isNotEmpty()) {
                    AsyncImage(
                        model = room.otherParticipant.profileUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(50.dp)
                            .align(Alignment.CenterVertically)
                            .clickable(onClick = { isExpanded = true }),
                        contentScale = ContentScale.Crop
                    )
                    if (isExpanded) {
                        FullScreenImageViewer(room.otherParticipant.profileUrl) {
                            isExpanded = false
                        }
                    }
                } else {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(52.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(onClick = {
                                showToast(
                                    context = context,
                                    "No profile picture"
                                )
                            }),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(
                    modifier = Modifier.fillMaxWidth(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = room.otherParticipant.username,
                        modifier = Modifier.padding(start = 7.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (room.lastMessage.isNotEmpty()) {
                        Text(
                            text = if (room.lastMessageSenderId == currentUserId) "You: ${room.lastMessage}" else room.lastMessage,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 7.dp, end = 10.dp),
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
//            last message time and unread count
            Column(
                modifier = Modifier.fillMaxWidth(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                room.lastMessageTimestamp?.let {
                    Text(
                        text = formatMessageTime(it.toDate()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                if (unreadCount != 0) {
                    Text(
                        text = if (unreadCount < 99) unreadCount.toString() else "99+",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary, shape = CircleShape
                            )
                            .widthIn(20.dp, 40.dp)
                    )
                }
            }

        }
    }
}

//@Preview
//@Composable
//fun PrevChatItem() {
//    ChatListItem(
//        item, navController = rememberNavController(),
//        chatViewModel =
//    )
//}
//
//val item = RoomData(
//    roomId = "room_1",
//    lastMessage = "Hey, how's it going with that",
//    lastMessageTimestamp = Timestamp.now(),
//    lastMessageSenderId = "user_1",
//    otherParticipant = User(
//        userId = "user_1",
//        username = "Alice Johnson",
//        profileUrl = "",
//        deviceToken = "token_1"
//    )
//)

//val user = User(
//    userId = "user_1",
//    username = "Alice Johnson",
//    profileUrl = "",
//    otherUsersDeviceToken = "token_1"
//)
