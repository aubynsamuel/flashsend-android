package com.aubynsamuel.flashsend.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import androidx.core.net.toUri
import com.aubynsamuel.flashsend.MainActivity
import com.aubynsamuel.flashsend.R
import com.aubynsamuel.flashsend.functions.logger
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ReplyReceiver : BroadcastReceiver() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        val repository = NotificationRepository()
        val notificationId = intent.data?.toString() ?: return
        val roomId = intent.getStringExtra("roomId") ?: return
        val sendersUserId = intent.getStringExtra("sendersUserId") ?: ""
        val recipientsUserId = intent.getStringExtra("recipientsUserId") ?: ""

        // Retrieve the reply text from RemoteInput
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        remoteInput?.let {
            val replyText = it.getCharSequence(MainActivity.KEY_TEXT_REPLY)?.toString()
            if (!replyText.isNullOrBlank()) {
                GlobalScope.launch {
                    try {
                        repository.sendReply(
                            sendersUserId = sendersUserId,
                            recipientsUserId = recipientsUserId,
                            roomId = roomId,
                            replyText = replyText
                        )
                    } catch (e: Exception) {
                        logger("NetWorkError", e.message.toString())
                    }
                }

                // Add the reply to the conversation history
                val newMessage = NotificationCompat.MessagingStyle.Message(
                    replyText, System.currentTimeMillis(), "Me"
                )
                ConversationHistoryManager.addMessage(notificationId, newMessage)

                // Rebuild and update the notification immediately
                updateNotification(context, notificationId, roomId, sendersUserId, recipientsUserId)
            }
        }
    }

    private fun updateNotification(
        context: Context,
        notificationId: String,
        roomId: String,
        sendersUserId: String,
        recipientsUserId: String
    ) {
        val messagingStyle = NotificationCompat.MessagingStyle("Me")
        ConversationHistoryManager.getHistory(notificationId)
            .forEach { it -> messagingStyle.addMessage(it) }

        val replyIntent = Intent(context, ReplyReceiver::class.java).apply {
            data = notificationId.toString().toUri()
            putExtra("sendersUserId", sendersUserId)
            putExtra("recipientsUserId", recipientsUserId)
            putExtra("roomId", roomId)
        }
        val replyPendingIntent = PendingIntent.getBroadcast(
            context, 1, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        val remoteInput = RemoteInput.Builder(MainActivity.KEY_TEXT_REPLY).setLabel("Reply").build()
        val replyAction = NotificationCompat.Action.Builder(
            R.mipmap.ic_launcher_foreground, "Reply", replyPendingIntent
        ).addRemoteInput(remoteInput).build()

        val markAsReadIntent = Intent(context, ActionReceiver::class.java).apply {
            action = "MARK_AS_READ"
            data = notificationId.toUri()
            putExtra("sendersUserId", sendersUserId)
            putExtra("recipientsUserId", recipientsUserId)
            putExtra("roomId", roomId)
        }
        val markAsReadPendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            markAsReadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val updatedNotification = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setStyle(messagingStyle)
            .addAction(replyAction)
            .addAction(R.mipmap.ic_launcher_foreground, "Mark As Read", markAsReadPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(groupKey)
            .build()

        val groupSummary = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle("New Messages")
            .setContentText("You have new messages")
            .setAutoCancel(true)
            .setGroup(groupKey)
            .setGroupSummary(true)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId.hashCode(), updatedNotification)
        notificationManager.notify(groupKey.hashCode(), groupSummary)
    }
}


object ConversationHistoryManager {
    // Map of conversationId to a list of messages
    private val conversationHistories =
        mutableMapOf<String, MutableList<NotificationCompat.MessagingStyle.Message>>()

    fun getHistory(conversationId: String): MutableList<NotificationCompat.MessagingStyle.Message> {
        return conversationHistories.getOrPut(conversationId) { mutableListOf() }
    }

    fun addMessage(conversationId: String, message: NotificationCompat.MessagingStyle.Message) {
        getHistory(conversationId).add(message)
    }

    fun hasMessages(conversationId: String): Boolean {
        return getHistory(conversationId).isNotEmpty()
    }
}