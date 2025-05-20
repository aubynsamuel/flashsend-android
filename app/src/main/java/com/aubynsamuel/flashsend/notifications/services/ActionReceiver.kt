package com.aubynsamuel.flashsend.notifications.services

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aubynsamuel.flashsend.core.domain.logger
import com.aubynsamuel.flashsend.notifications.domain.NotificationRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ActionReceiver : BroadcastReceiver() {
    private val tag = "ActionReceiver"

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        val repository = NotificationRepository()
        val notificationId = intent.data
        val roomId = intent.getStringExtra("roomId") ?: return
        val sendersUserId = intent.getStringExtra("sendersUserId") ?: ""

        if (intent.action == "MARK_AS_READ") {
            GlobalScope.launch {
                try {
                    repository.markMessagesAsRead(
                        sendersUserId = sendersUserId,
                        roomId = roomId,
                    )
                } catch (e: Exception) {
                    logger(tag, e.message.toString())
                }
            }

            ConversationHistoryManager.getHistory(notificationId.toString()).clear()

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.cancel(notificationId.hashCode())
        }
    }
}