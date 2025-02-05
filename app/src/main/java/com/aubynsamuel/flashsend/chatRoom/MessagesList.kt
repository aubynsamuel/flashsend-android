package com.aubynsamuel.flashsend.chatRoom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MessagesList(
    messages: List<Message>,
    modifier: Modifier = Modifier,
    scrollState: LazyListState
) {
    LazyColumn(
        modifier = modifier,
        state = scrollState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        reverseLayout = false,
        contentPadding = PaddingValues(top = 10.dp, bottom = 10.dp)
    ) {
        items(messages, key = { it.time }) { message ->
            ChatMessage(
                message = message,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = if (message.isFromMe) 30.dp else 0.dp,
                        end = if (message.isFromMe) 0.dp else 30.dp
                    )
            )
        }
    }
}