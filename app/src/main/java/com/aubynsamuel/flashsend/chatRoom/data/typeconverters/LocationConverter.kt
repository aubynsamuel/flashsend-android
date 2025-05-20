package com.aubynsamuel.flashsend.chatRoom.data.typeconverters

import androidx.room.TypeConverter
import com.aubynsamuel.flashsend.core.domain.logger
import com.aubynsamuel.flashsend.core.model.Location

class LocationConverter {
    @TypeConverter
    fun fromString(value: String?): Location? {
        if (value == null) {
//            Log.d("LocationConverter", "fromString: Received null value, returning null Location")
            return null
        }
        return try {
            val parts = value.split(",")
            val location = Location(parts[0].toDouble(), parts[1].toDouble())
//            Log.d(
//                "LocationConverter",
//                "fromString: Converted string '$value' to Location: $location"
//            )
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
//        Log.d("LocationConverter", "toString: Converted Location $location to string: $result")
        return result
    }
}