package com.aubynsamuel.flashsend.chatRoom.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aubynsamuel.flashsend.chatRoom.domain.ChatViewModel
import com.aubynsamuel.flashsend.core.model.ChatMessage
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessagesList(
    messages: List<ChatMessage>,
    currentUserId: String,
    modifier: Modifier = Modifier,
    scrollState: LazyListState,
    roomId: String,
    fontSize: Int,
    chatViewModel: ChatViewModel
) {
    val groupedMessages = remember(messages) {
        messages.groupBy { message ->
            SimpleDateFormat("EEEE dd MMM yyyy", Locale.getDefault()).format(message.createdAt)
        }
    }

    LazyColumn(
        modifier = modifier,
        state = scrollState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        reverseLayout = true,
        contentPadding = PaddingValues(top = 10.dp, bottom = 10.dp)
    ) {
        groupedMessages.forEach { (date, messagesForDate) ->
            items(
                count = messagesForDate.size,
                key = { messagesForDate[it].id }
            ) { index ->
                val message = messagesForDate[index]
                ChatMessageObject(
                    message = message,
                    isFromMe = message.senderId == currentUserId,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = if (message.senderId == currentUserId) 60.dp else 0.dp,
                            end = if (message.senderId == currentUserId) 0.dp else 60.dp
                        ),
                    roomId = roomId,
                    fontSize = fontSize,
                    chatViewModel = chatViewModel,
                    currentUserId = currentUserId
                )
            }
            // Header for Date
            item {
                DateHeader(date)
            }
        }
    }
}


@Composable
fun DateHeader(date: String) {
    Surface(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
        color = Color.Transparent
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White, textAlign = TextAlign.Center
        )
    }
}