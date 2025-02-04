package com.aubynsamuel.flashsend.chatRoom

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aubynsamuel.flashsend.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun ChatScreen() {
    val context = LocalContext.current
    var messageList = generateMockMessages()
    var messageText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf(*messageList.toTypedArray()) }
    val coroutineScope = rememberCoroutineScope()

    // Initialize the scroll state so that the last item is visible from the start.
    val initialIndex = if (messages.isNotEmpty()) messages.size - 1 else 0
    val scrollState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    // Remember the previous count to detect additions
    var previousMessageCount by remember { mutableIntStateOf(messages.size) }

    // Animate to the bottom only if new messages are added.
    LaunchedEffect(Unit) {
//        if (messages.size > previousMessageCount) {
//            scrollState.animateScrollToItem(messages.size - 1)
//        }
//        previousMessageCount = messages.size
        while (false) {
            messages.add(
                Message(
                    text = "Automatic message sent programmatically",
                    time = System.currentTimeMillis(),
                    isFromMe = false
                )
            )
            scrollState.animateScrollToItem(messages.size - 1)
            vibrateDevice(context)
            vibrateDevice(context)
            delay(5000)
        }
    }
    Column {

        HeaderBar(name = "Samuel", pic = R.drawable.ic_launcher_foreground)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 20.dp)
        ) {
            MessagesList(
                messages = messages,
                modifier = Modifier.weight(1f),
                scrollState = scrollState
            )

//        ScrollToBottom(onClick = {
//            coroutineScope.launch { scrollState.animateScrollToItem(messages.size - 1) }
//        }, modifier = Modifier
//            .absolutePadding(20.dp)
//            .align(Alignment.End))

            MessageInput(
                messageText = messageText,
                onMessageChange = { messageText = it },
                onSend = {
                    if (messageText.isNotBlank()) {
                        messages.add(
                            Message(
                                text = messageText,
                                time = System.currentTimeMillis(),
                                isFromMe = true
                            )
                        )
                        messageText = ""
                        previousMessageCount = messages.size
                        vibrateDevice(context)
                        coroutineScope.launch { scrollState.animateScrollToItem(messages.size - 1) }
                    }
                }
            )
        }
    }
}


data class Message(
    val text: String,
    val time: Long,
    val isFromMe: Boolean
)

@RequiresApi(Build.VERSION_CODES.R)
@Preview(showBackground = true)
@Composable
fun PreviewChatScreen() {
    MaterialTheme {
        ChatScreen()
    }
}

@Preview
@Composable
fun PreviewMessageFromMe() {
    MaterialTheme {
        ChatMessage(
            message = Message(
                text = "Hello! This is my message",
                time = System.currentTimeMillis(),
                isFromMe = true
            )
        )
    }
}

@Preview
@Composable
fun PreviewMessageFromOther() {
    MaterialTheme {
        ChatMessage(
            message = Message(
                text = "Hi! This is a received message",
                time = System.currentTimeMillis(),
                isFromMe = false
            )
        )
    }
}

// Generates realistic mock messages with random timestamps
fun generateMockMessages(): List<Message> {
    val messages = mutableListOf<Message>()
    val baseTime = System.currentTimeMillis()
    val random = Random(System.currentTimeMillis())

    // Add initial message
    messages.add(
        Message(
            text = "Hey! How are you?",
            time = baseTime - 3600000, // 1 hour ago
            isFromMe = false
        )
    )

    // Generate conversation-like messages
    val conversation = listOf(
        "First Message",
        "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!",
        "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!", "I'm doing great, thanks! How about you?",
        "Pretty good! Any plans for the weekend?",
        "Not much, maybe go hiking. You?",
        "Sounds fun! I might check out the new cafe downtown",
        "Oh which one? The place on Main Street?",
        "Yes, that's the one! Heard they have great coffee",
        "We should go together sometime!",
        "Definitely! How about next Wednesday?",
        "Works for me! Let's meet at 2pm?",
        "Perfect 👍 See you then!",
        "Last Message"
    )

    var currentTime = baseTime - 1800000 // 30 minutes ago
    var isFromMe = true

    conversation.forEach { text ->
        messages.add(
            Message(
                text = text,
                time = currentTime,
                isFromMe = isFromMe
            )
        )
        currentTime += 60000 * random.nextInt(4) + 1 // Add 1-5 minutes between messages
        isFromMe = !isFromMe // Alternate sender
    }

    return messages
}