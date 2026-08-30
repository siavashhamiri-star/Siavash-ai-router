package com.example.tavanacity.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tavanacity.domain.model.MessageSender

@Entity(tableName = "chat_vault_messages")
data class VaultMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val sender: String, // MessageSender.USER.name, AI.name, SYSTEM.name
    val personaId: String,
    val personaTitle: String,
    val timestamp: Long = System.currentTimeMillis(),
    val providerUsed: String? = null,
    val latencyMs: Long? = null,
    val isError: Boolean = false,
    val isReported: Boolean = false,
    val reportReason: String? = null
) {
    fun isUser(): Boolean = sender == MessageSender.USER.name
    fun isAI(): Boolean = sender == MessageSender.AI.name
    fun isSystem(): Boolean = sender == MessageSender.SYSTEM.name
}
