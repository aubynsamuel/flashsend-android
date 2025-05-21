package com.aubynsamuel.flashsend.notifications.domain

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.net.toUri
import com.aubynsamuel.flashsend.MainActivity
import com.aubynsamuel.flashsend.R
import com.aubynsamuel.flashsend.notifications.data.services.ActionReceiver
import com.aubynsamuel.flashsend.notifications.data.services.ConversationHistoryManager
import com.aubynsamuel.flashsend.notifications.data.services.ReplyReceiver
import com.aubynsamuel.flashsend.notifications.data.services.generateSender
import com.aubynsamuel.flashsend.notifications.data.services.person

fun showNotification(
    context: Context,
    message: String,
    sender: String = "Unknown",
    id: String,
    sendersUserId: String,
    recipientsUserId: String,
) {
    // Create an intent for opening the app
    val contentIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        data = id.toString().toUri()
    }
    val contentPendingIntent = PendingIntent.getActivity(
        context,
        0,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Create the reply action
    val replyIntent = Intent(context, ReplyReceiver::class.java).apply {
        data = id.toString().toUri()
        putExtra("sendersUserId", sendersUserId)
        putExtra("recipientsUserId", recipientsUserId)
        putExtra("roomId", id)
    }
    val replyPendingIntent = PendingIntent.getBroadcast(
        context,
        1,
        replyIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )
    // Create RemoteInput for inline replies
    val remoteInput = RemoteInput.Builder(MainActivity.KEY_TEXT_REPLY)
        .setLabel("Reply")
        .build()
    val replyAction = NotificationCompat.Action.Builder(
        R.mipmap.ic_launcher_round, "Reply", replyPendingIntent
    )
        .addRemoteInput(remoteInput)
        .build()

    // Create a "Mark as Read" action
    val markAsReadIntent = Intent(context, ActionReceiver::class.java).apply {
        action = "MARK_AS_READ"
        data = id.toString().toUri()
        putExtra("sendersUserId", sendersUserId)
        putExtra("recipientsUserId", recipientsUserId)
        putExtra("roomId", id)

    }
    val markAsReadPendingIntent = PendingIntent.getBroadcast(
        context,
        2,
        markAsReadIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val newMessage = NotificationCompat.MessagingStyle.Message(
        message, System.currentTimeMillis(), generateSender(name = sender)
    )
    ConversationHistoryManager.addMessage(id.toString(), newMessage)
    // Rebuild the MessagingStyle notification with the full conversation history.
    val messagingStyle = NotificationCompat.MessagingStyle(person)

    // Append each message in the history
    ConversationHistoryManager.getHistory(id.toString()).forEach { message ->
        messagingStyle.addMessage(message)
    }

    val individualNotification = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher_round)
        .setStyle(messagingStyle)
        .setAutoCancel(true)
        .setContentIntent(contentPendingIntent)
        .addAction(replyAction)
        .addAction(R.mipmap.ic_launcher_round, "Mark As Read", markAsReadPendingIntent)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setGroup(groupKey)
        .build()

    val groupSummary = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher_round)
        .setContentTitle("New Messages")
        .setContentText("You have new messages")
        .setAutoCancel(true)
        .setGroup(groupKey)
        .setGroupSummary(true)
        .build()

    NotificationManagerCompat.from(context).apply {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notify(id.hashCode(), individualNotification)
            notify(groupKey.hashCode(), groupSummary)
        }
    }
}

const val groupKey = "chat_messages_group"