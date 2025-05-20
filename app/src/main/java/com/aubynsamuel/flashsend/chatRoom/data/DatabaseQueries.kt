package com.aubynsamuel.flashsend.chatRoom.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

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

    @Query("UPDATE messages SET content = :content WHERE id = :messageId")
    suspend fun editMessage(messageId: String, content: String)

    @Query("SELECT * FROM messages WHERE roomId = :roomId AND createdAt > :timestamp ORDER BY createdAt DESC")
    fun getNewMessages(roomId: String, timestamp: Long): Flow<List<MessageEntity>>

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)
}