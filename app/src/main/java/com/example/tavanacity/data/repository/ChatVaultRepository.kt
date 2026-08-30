package com.example.tavanacity.data.repository

import com.example.tavanacity.data.local.MessageDao
import com.example.tavanacity.data.local.VaultMessageEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ChatVaultRepository(
    private val messageDao: MessageDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    val messagesFlow: Flow<List<VaultMessageEntity>> = messageDao.getAllMessagesFlow()

    suspend fun saveMessage(message: VaultMessageEntity): Long = withContext(ioDispatcher) {
        messageDao.insertMessage(message)
    }

    suspend fun updateMessage(message: VaultMessageEntity) = withContext(ioDispatcher) {
        messageDao.updateMessage(message)
    }

    suspend fun reportMessage(id: Long, reason: String) = withContext(ioDispatcher) {
        messageDao.markMessageAsReported(id, reason)
    }

    suspend fun deleteMessage(id: Long) = withContext(ioDispatcher) {
        messageDao.deleteMessageById(id)
    }

    suspend fun clearVault() = withContext(ioDispatcher) {
        messageDao.clearAllMessages()
    }

    suspend fun getAllMessages(): List<VaultMessageEntity> = withContext(ioDispatcher) {
        messageDao.getAllMessages()
    }
}
