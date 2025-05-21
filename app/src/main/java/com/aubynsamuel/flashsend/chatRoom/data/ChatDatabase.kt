package com.aubynsamuel.flashsend.chatRoom.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aubynsamuel.flashsend.chatRoom.data.typeconverters.DateConverter
import com.aubynsamuel.flashsend.chatRoom.data.typeconverters.LocationConverter
import com.aubynsamuel.flashsend.chatRoom.data.typeconverters.ReactionConverter

internal const val ChatDataBaseLogs = "ChatDatabase"

@Database(entities = [MessageEntity::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class, LocationConverter::class, ReactionConverter::class)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: ChatDatabase? = null

        fun getDatabase(context: Context): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                Log.d(ChatDataBaseLogs, "Creating new ChatDatabase instance")
                val instance = Room.databaseBuilder(
                    context.applicationContext, ChatDatabase::class.java, "chat_database"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Log.d(ChatDataBaseLogs, "onCreate: Database created at path: ${db.path}")
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        Log.d(ChatDataBaseLogs, "onOpen: Database opened at path: ${db.path}")
                    }
                }).build()
                INSTANCE = instance
                Log.d(ChatDataBaseLogs, "ChatDatabase instance created and assigned")
                instance
            }
        }
    }
}
