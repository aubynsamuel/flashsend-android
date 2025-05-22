package com.aubynsamuel.flashsend.chatRoom.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.aubynsamuel.flashsend.chatRoom.data.local.typeconverters.DateConverter
import com.aubynsamuel.flashsend.chatRoom.data.local.typeconverters.LocationConverter
import com.aubynsamuel.flashsend.chatRoom.data.local.typeconverters.ReactionConverter
import com.aubynsamuel.flashsend.core.model.Location
import java.util.Date

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val content: String,
    val image: String?,
    val audio: String?,
    @TypeConverters(DateConverter::class)
    val createdAt: Date,
    val senderId: String,
    val senderName: String,
    val replyTo: String?,
    val read: Boolean,
    val type: String,
    val delivered: Boolean,
    @TypeConverters(LocationConverter::class)
    val location: Location?,
    val duration: Long?,
    val roomId: String,
    @TypeConverters(ReactionConverter::class)
    val reactions: MutableMap<String, String> = mutableMapOf(),
)