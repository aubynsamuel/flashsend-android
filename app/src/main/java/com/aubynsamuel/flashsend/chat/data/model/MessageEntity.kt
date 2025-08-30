package com.aubynsamuel.flashsend.chat.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.aubynsamuel.flashsend.chat.data.local.typeconverters.DateConverter
import com.aubynsamuel.flashsend.chat.data.local.typeconverters.LocationConverter
import com.aubynsamuel.flashsend.chat.data.local.typeconverters.ReactionConverter
import com.aubynsamuel.flashsend.core.domain.model.Location
import java.util.Date

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val content: String,
    val image: String?,
    val audio: String?,
    @param:TypeConverters(DateConverter::class)
    val createdAt: Date,
    val senderId: String,
    val senderName: String,
    val replyTo: String?,
    val read: Boolean,
    val type: String,
    val delivered: Boolean,
    @param:TypeConverters(LocationConverter::class)
    val location: Location?,
    val duration: Long?,
    val roomId: String,
    @param:TypeConverters(ReactionConverter::class)
    val reactions: Map<String, String> = emptyMap(),
)