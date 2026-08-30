package com.example.tavanacity.data.provider

import com.example.tavanacity.domain.model.AIPersona
import com.example.tavanacity.domain.model.AIResponse
import com.example.tavanacity.domain.model.RouterError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FallbackAIProviderAdapter(
    private val secondaryProvider: AIProvider? = null
) : AIProvider {

    override val id: String = "fallback_orchestrator"
    override val displayName: String = "Fallback Edge AI"

    override suspend fun generateResponse(
        prompt: String,
        persona: AIPersona,
        timeoutMs: Long
    ): Result<AIResponse> = withContext(Dispatchers.IO) {
        if (secondaryProvider != null) {
            val result = secondaryProvider.generateResponse(prompt, persona, timeoutMs)
            if (result.isSuccess) {
                return@withContext result
            }
        }

        // If no real secondary AI provider exists or secondary provider failed,
        // we return a structured AIUnavailable failure without producing any fake AI text.
        Result.failure(
            RouterError.AIUnavailable(
                technicalLog = "Secondary real AI fallback provider is not configured or failed to produce a valid response.",
                userMessageFa = "سرویس هوش مصنوعی پشتیبان در دسترس نیست."
            )
        )
    }
}
