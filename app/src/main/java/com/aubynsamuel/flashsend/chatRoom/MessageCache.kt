package com.aubynsamuel.flashsend.chatRoom

import android.content.Context
import android.util.Log
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aubynsamuel.flashsend.logger
import kotlinx.coroutines.flow.Flow
import java.util.Date

private const val TAG = "ChatDatabase"

// Room Entity
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,
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
    val roomId: String // Added to associate messages with rooms
)

// Type Converters
class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        val date = value?.let { Date(it) }
        Log.d("DateConverter", "fromTimestamp: Converting timestamp $value to Date: $date")
        return date
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        val timestamp = date?.time
        Log.d("DateConverter", "dateToTimestamp: Converting Date $date to timestamp: $timestamp")
        return timestamp
    }
}

class LocationConverter {
    @TypeConverter
    fun fromString(value: String?): Location? {
        if (value == null) {
            Log.d("LocationConverter", "fromString: Received null value, returning null Location")
            return null
        }
        return try {
            val parts = value.split(",")
            val location = Location(parts[0].toDouble(), parts[1].toDouble())
            Log.d(
                "LocationConverter",
                "fromString: Converted string '$value' to Location: $location"
            )
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
        Log.d("LocationConverter", "toString: Converted Location $location to string: $result")
        return result
    }
}

// DAO
@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE roomId = :roomId ORDER BY createdAt DESC")
    fun getMessagesForRoom(roomId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("UPDATE messages SET read = :read WHERE id = :messageId")
    suspend fun updateMessageReadStatus(messageId: String, read: Boolean)

    @Query("SELECT * FROM messages WHERE roomId = :roomId AND createdAt > :timestamp ORDER BY createdAt DESC")
    fun getNewMessages(roomId: String, timestamp: Long): Flow<List<MessageEntity>>
}

// Database
@Database(entities = [MessageEntity::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class, LocationConverter::class)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: ChatDatabase? = null

        fun getDatabase(context: Context): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                Log.d(TAG, "Creating new ChatDatabase instance")
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    "chat_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d(TAG, "onCreate: Database created at path: ${db.path}")
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            Log.d(TAG, "onOpen: Database opened at path: ${db.path}")
                        }
                    })
                    .build()
                INSTANCE = instance
                Log.d(TAG, "ChatDatabase instance created and assigned")
                instance
            }
        }
    }
}
