package com.aubynsamuel.flashsend.chatRoom.data.typeconverters

import androidx.room.TypeConverter
import java.util.Date

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        val date = value?.let { Date(it) }
//        Log.d("DateConverter", "fromTimestamp: Converting timestamp $value to Date: $date")
        return date
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        val timestamp = date?.time
//        Log.d("DateConverter", "dateToTimestamp: Converting Date $date to timestamp: $timestamp")
        return timestamp
    }
}