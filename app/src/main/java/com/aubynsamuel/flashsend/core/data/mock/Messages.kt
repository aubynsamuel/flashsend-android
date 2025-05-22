package com.aubynsamuel.flashsend.core.data.mock

import com.aubynsamuel.flashsend.core.model.ChatMessage
import java.util.Date
import java.util.Random

@Suppress("unused", "UnusedVariable")
fun generateMockMessages(parsedId: String): List<ChatMessage> {
    val messages = mutableListOf<ChatMessage>()
    val baseTime = Date()
    val random = Random(System.currentTimeMillis())

    // Add initial message
    messages.add(
        ChatMessage(
            id = "12",
            content = "Whats up with the new shoe",
            createdAt = Date(),
            senderId = "",
            senderName = "",
            read = true,
            delivered = true
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
        "Last Message"
    )

    var currentTime = Date()
    var isFromMe = true
    var senderId = ""
    var id = 0

    conversation.forEach { text ->
        isFromMe = !isFromMe
        senderId = if (isFromMe) {
            parsedId
        } else {
            ""
        }
        id++
        messages.add(
            ChatMessage(
                id = id.toString(),
                content = text,
                createdAt = currentTime,
                senderId = senderId,
                senderName = "",
                read = true,
                delivered = true
            )
        )
    }

    return messages
}
