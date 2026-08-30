package com.example.tavanacity.domain.model

data class AIResponse(
    val text: String,
    val latencyMs: Long,
    val providerName: String,
    val tokensUsed: Int? = null
)

enum class MessageSender {
    USER,
    AI,
    SYSTEM
}
