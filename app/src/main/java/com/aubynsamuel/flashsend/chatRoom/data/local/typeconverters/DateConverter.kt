package com.aubynsamuel.flashsend.chatRoom.data.local.typeconverters

import androidx.room.TypeConverter
import java.util.Date

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        val date = value?.let { Date(it) }
        return date
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        val timestamp = date?.time
        return timestamp
    }
}