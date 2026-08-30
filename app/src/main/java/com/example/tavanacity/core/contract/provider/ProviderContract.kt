package com.example.tavanacity.core.contract.provider

import com.squareup.moshi.JsonClass

/**
 * Platform-Neutral Model Tier taxonomy for multi-provider routing.
 */
@JsonClass(generateAdapter = false)
enum class ModelTierDTO {
    ECONOMIC,
    STANDARD,
    ADVANCED,
    FLAGSHIP
}

/**
 * Platform-Neutral Provider request payload.
 */
@JsonClass(generateAdapter = true)
data class ProviderRequestDTO(
    val prompt: String,
    val systemInstruction: String? = null,
    val personaId: String,
    val requestedTier: ModelTierDTO = ModelTierDTO.STANDARD,
    val maxTokens: Int = 2048,
    val temperature: Float = 0.7f,
    val streamResponse: Boolean = false,
    val conversationId: String? = null
)

/**
 * Platform-Neutral Provider response payload.
 */
@JsonClass(generateAdapter = true)
data class ProviderResponseDTO(
    val content: String,
    val providerUsed: String,
    val modelIdentifier: String,
    val tokensConsumed: Int,
    val latencyMs: Long,
    val isSafetyPassed: Boolean,
    val finishReason: String = "STOP",
    val errorDetails: String? = null
)
