package com.aubynsamuel.flashsend.chatRoom.data.typeconverters

import androidx.room.TypeConverter
import com.aubynsamuel.flashsend.core.domain.logger
import com.aubynsamuel.flashsend.core.model.Location

class LocationConverter {
    @TypeConverter
    fun fromString(value: String?): Location? {
        if (value == null) {
            return null
        }
        return try {
            val parts = value.split(",")
            val location = Location(parts[0].toDouble(), parts[1].toDouble())
            location
        } catch (e: Exception) {
            logger(
                "LocationConverter",
                "fromString: Error converting string '$value' to Location $e",
            )
            null
        }
    }

    @TypeConverter
    fun toString(location: Location?): String? {
        val result = location?.let { "${it.latitude},${it.longitude}" }
        return result
    }
}