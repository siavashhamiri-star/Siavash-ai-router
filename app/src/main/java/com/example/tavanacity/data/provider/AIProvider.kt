package com.example.tavanacity.data.provider

import com.example.tavanacity.domain.model.AIPersona
import com.example.tavanacity.domain.model.AIResponse

interface AIProvider {
    val id: String
    val displayName: String

    /**
     * Generates an AI response given the user prompt, persona system prompt, and specific timeout.
     * Returns Result.success(AIResponse) or Result.failure(RouterError).
     */
    suspend fun generateResponse(
        prompt: String,
        persona: AIPersona,
        timeoutMs: Long
    ): Result<AIResponse>
}
