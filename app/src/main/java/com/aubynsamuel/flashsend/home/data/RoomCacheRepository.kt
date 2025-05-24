package com.aubynsamuel.flashsend.home.data

import android.content.Context
import androidx.core.content.edit
import com.aubynsamuel.flashsend.core.domain.logger
import com.aubynsamuel.flashsend.core.model.RoomData
import com.google.firebase.Timestamp
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class RoomsCache(context: Context) {
    private val tag = "roomCache"
    private val sharedPreferences = context.getSharedPreferences("app_cache", Context.MODE_PRIVATE)
    private val gson = GsonBuilder()
        .registerTypeAdapter(Timestamp::class.java, TimestampTypeAdapter())
        .create()

    fun saveRooms(rooms: List<RoomData>) {
        val json = gson.toJson(rooms)
        sharedPreferences.edit { putString("cached_rooms", json) }
    }

    fun loadRooms(): List<RoomData> {
        return try {
            val json = sharedPreferences.getString("cached_rooms", null)
            json?.let {
                val type = object : TypeToken<List<RoomData>>() {}.type
                gson.fromJson(it, type) ?: emptyList()
            } ?: emptyList()
        } catch (e: Exception) {
            logger(tag, e.message.toString())
            emptyList()
        }
    }

    fun clearRooms() {
        sharedPreferences.edit { remove("cached_rooms") }
    }

    private class TimestampTypeAdapter : JsonSerializer<Timestamp>, JsonDeserializer<Timestamp> {
        override fun serialize(
            src: Timestamp,
            typeOfSrc: Type,
            context: JsonSerializationContext,
        ): JsonElement {
            return JsonObject().apply {
                addProperty("seconds", src.seconds)
                addProperty("nanoseconds", src.nanoseconds)
            }
        }

        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext,
        ): Timestamp {
            val jsonObject = json.asJsonObject
            return Timestamp(
                jsonObject.get("seconds").asLong,
                jsonObject.get("nanoseconds").asInt
            )
        }
    }
}