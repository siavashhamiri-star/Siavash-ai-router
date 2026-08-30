package com.example.tavanacity.data.provider

import com.example.tavanacity.domain.model.AIPersona
import com.example.tavanacity.domain.model.AIResponse
import com.example.tavanacity.domain.model.RouterError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalOfflineProviderAdapter : AIProvider {

    override val id: String = "local_offline_stub"
    override val displayName: String = "Local (Offline Mode)"

    override suspend fun generateResponse(
        prompt: String,
        persona: AIPersona,
        timeoutMs: Long
    ): Result<AIResponse> = withContext(Dispatchers.Default) {
        // Honest technical handling: No on-device local LLM engine is bundled in this release.
        // Return a structured failure rather than any simulated response.
        Result.failure(
            RouterError.AIUnavailable(
                technicalLog = "No on-device local LLM model engine is installed in this runtime.",
                userMessageFa = "سرویس هوش مصنوعی محلی در این نسخه در دسترس نیست. لطفاً اتصال اینترنت خود را متصل نمایید."
            )
        )
    }
}
