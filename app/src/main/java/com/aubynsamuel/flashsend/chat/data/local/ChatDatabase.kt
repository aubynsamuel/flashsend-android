package com.aubynsamuel.flashsend.chat.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aubynsamuel.flashsend.chat.data.local.typeconverters.DateConverter
import com.aubynsamuel.flashsend.chat.data.local.typeconverters.LocationConverter
import com.aubynsamuel.flashsend.chat.data.local.typeconverters.ReactionConverter
import com.aubynsamuel.flashsend.chat.data.model.MessageEntity


@Database(entities = [MessageEntity::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class, LocationConverter::class, ReactionConverter::class)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao

    companion object {
        private const val CHAT_DATABASE_LOGS = "ChatDatabase"

        @Volatile
        private var INSTANCE: ChatDatabase? = null

        fun getDatabase(context: Context): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                Log.d(CHAT_DATABASE_LOGS, "Creating new ChatDatabase instance")
                val instance = Room.databaseBuilder(
                    context.applicationContext, ChatDatabase::class.java, "chat_database"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Log.d(CHAT_DATABASE_LOGS, "onCreate: Database created at path: ${db.path}")
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        Log.d(CHAT_DATABASE_LOGS, "onOpen: Database opened at path: ${db.path}")
                    }
                }).build()
                INSTANCE = instance
                Log.d(CHAT_DATABASE_LOGS, "ChatDatabase instance created and assigned")
                instance
            }
        }
    }
}
