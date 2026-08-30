package com.example.tavanacity.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM chat_vault_messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<VaultMessageEntity>>

    @Query("SELECT * FROM chat_vault_messages ORDER BY timestamp ASC")
    suspend fun getAllMessages(): List<VaultMessageEntity>

    @Query("SELECT * FROM chat_vault_messages ORDER BY timestamp ASC")
    fun getAllMessagesSync(): List<VaultMessageEntity>

    @Query("SELECT * FROM chat_vault_messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: Long): VaultMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: VaultMessageEntity): Long

    @Update
    suspend fun updateMessage(message: VaultMessageEntity)

    @Query("UPDATE chat_vault_messages SET isReported = 1, reportReason = :reason WHERE id = :id")
    suspend fun markMessageAsReported(id: Long, reason: String)

    @Query("DELETE FROM chat_vault_messages WHERE id = :id")
    suspend fun deleteMessageById(id: Long)

    @Query("DELETE FROM chat_vault_messages")
    suspend fun clearAllMessages()
}
