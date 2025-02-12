package com.aubynsamuel.flashsend.chatRoom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aubynsamuel.flashsend.functions.ChatMessage
import kotlinx.coroutines.CoroutineScope

@Composable
fun MessagesList(
    messages: List<ChatMessage>,
    currentUserId: String,
    modifier: Modifier = Modifier,
    scrollState: LazyListState,
    coroutineScope: CoroutineScope,
    roomId: String,
    fontSize: Int
) {
    LazyColumn(
        modifier = modifier,
        state = scrollState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        reverseLayout = true,
        contentPadding = PaddingValues(top = 10.dp, bottom = 10.dp)
    ) {
        items(
            count = messages.size,
            key = { messages[it].id }
        ) { index ->
            val message = messages[index]
            ChatMessageObject(
                message = message,
                isFromMe = message.senderId == currentUserId,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = if (message.senderId == currentUserId) 60.dp else 0.dp,
                        end = if (message.senderId == currentUserId) 0.dp else 60.dp
                    ),
                coroutineScope = coroutineScope,
                roomId = roomId,
                fontSize = fontSize
            )
        }
    }
}
