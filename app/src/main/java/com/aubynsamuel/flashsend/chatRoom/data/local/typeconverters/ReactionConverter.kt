package com.aubynsamuel.flashsend.chatRoom.data.local.typeconverters

import androidx.annotation.Keep
import androidx.room.TypeConverter

@Keep
class ReactionConverter() {
    @TypeConverter
    fun fromReactionsMap(reactions: Map<String, String>?): String? {
        if (reactions == null) return null

        return reactions.entries.joinToString(",") { (key, value) ->
            "${key.replace(":", "\\:").replace(",", "\\,")}:${
                value.replace(":", "\\:").replace(",", "\\,")
            }"
        }
    }

    @TypeConverter
    fun toReactionsMap(reactionsString: String?): Map<String, String>? {
        if (reactionsString.isNullOrEmpty()) return emptyMap()

        return try {
            reactionsString.split(",").associate { pair ->
                val keyValue = pair.split(":")
                if (keyValue.size != 2) {
                    throw IllegalArgumentException("Invalid key-value pair: $pair")
                }
                val key = keyValue[0].replace("\\:", ":").replace("\\,", ",")
                val value = keyValue[1].replace("\\:", ":").replace("\\,", ",")
                key to value
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

}