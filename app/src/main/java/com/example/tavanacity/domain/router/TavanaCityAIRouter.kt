package com.example.tavanacity.domain.router

import android.util.Log
import com.example.tavanacity.data.local.VaultMessageEntity
import com.example.tavanacity.data.network.NetworkMonitor
import com.example.tavanacity.data.provider.AIProvider
import com.example.tavanacity.data.provider.CloudAIProviderAdapter
import com.example.tavanacity.data.provider.FallbackAIProviderAdapter
import com.example.tavanacity.data.provider.LocalOfflineProviderAdapter
import com.example.tavanacity.data.repository.AccountRepository
import com.example.tavanacity.data.repository.ChatVaultRepository
import com.example.tavanacity.data.safety.SafetyResult
import com.example.tavanacity.data.safety.TavanaCitySafetyLayer
import com.example.tavanacity.domain.model.AIPersona
import com.example.tavanacity.domain.model.AIResponse
import com.example.tavanacity.domain.model.MessageSender
import com.example.tavanacity.domain.model.NetworkStatus
import com.example.tavanacity.domain.model.RouterError
import com.example.tavanacity.domain.model.UserPlan
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class RouterState(
    val isRouting: Boolean = false,
    val activeProviderName: String = "Cloud AI (Gemini / Gateway)",
    val currentPersona: AIPersona = AIPersona.GENERAL_ASSISTANT,
    val networkStatus: NetworkStatus = NetworkStatus.ONLINE,
    val lastError: RouterError? = null,
    val lastRoutingDurationMs: Long? = null
)

class TavanaCityAIRouter(
    private val repository: ChatVaultRepository,
    private val networkMonitor: NetworkMonitor,
    private val safetyLayer: TavanaCitySafetyLayer = TavanaCitySafetyLayer(),
    private val primaryProvider: AIProvider = CloudAIProviderAdapter(),
    private val fallbackProvider: AIProvider = FallbackAIProviderAdapter(),
    private val localOfflineProvider: AIProvider = LocalOfflineProviderAdapter(),
    private val accountRepository: AccountRepository? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val _routerState = MutableStateFlow(
        RouterState(networkStatus = networkMonitor.networkStatus.value)
    )
    val routerState: StateFlow<RouterState> = _routerState.asStateFlow()

    private val processMutex = Mutex()

    /**
     * Executes the mandatory pipeline:
     * 1. User Input Validation
     * 2. Entitlement & Quota Check (Server/Account Authority)
     * 3. Safety Check (SafetyLayer)
     * 4. Save User Message to Chat Vault
     * 5. Determine Real Network State
     * 6. Select Provider & Fallback Strategy
     * 7. Real Response Generation (Zero fake responses)
     * 8. Response Structure Validation
     * 9. Deduct Credits upon Verified Success
     * 10. Save Verified AI Response to Vault
     * 11. Update UI & Router State
     */
    suspend fun processMessage(
        rawInput: String,
        persona: AIPersona = _routerState.value.currentPersona
    ): Result<VaultMessageEntity> = withContext(ioDispatcher) {
        processMutex.withLock {
            val input = rawInput.trim()
            if (input.isBlank()) {
                val error = RouterError.UnknownError(
                    technicalLog = "Cannot process empty input string",
                    userMessageFa = "پیام نمی‌تواند خالی باشد."
                )
                _routerState.value = _routerState.value.copy(lastError = error)
                return@withContext Result.failure(error)
            }

            // 1.5: Entitlement & Credit Balance Check
            if (accountRepository != null) {
                val account = accountRepository.accountState.value
                val requiredTier = persona.modelTier
                val requiredCredits = requiredTier.creditCost

                // Check plan tier compatibility
                if (!account.plan.canAccessModelTier(requiredTier)) {
                    val minimumPlan = UserPlan.entries.firstOrNull { it.canAccessModelTier(requiredTier) } ?: UserPlan.PRO
                    val entitlementError = RouterError.EntitlementRequired(
                        requiredPlan = minimumPlan,
                        technicalLog = "User plan (${account.plan.name}) cannot access persona ${persona.id} requiring ${requiredTier.name}"
                    )
                    _routerState.value = _routerState.value.copy(lastError = entitlementError)
                    return@withContext Result.failure(entitlementError)
                }

                // Check credit balance
                if (!account.hasSufficientCredits(requiredCredits)) {
                    val creditError = RouterError.InsufficientCredit(
                        requiredCredits = requiredCredits,
                        currentBalance = account.creditBalance,
                        technicalLog = "Insufficient credits for persona ${persona.id} (needs $requiredCredits, has ${account.creditBalance})"
                    )
                    _routerState.value = _routerState.value.copy(lastError = creditError)
                    return@withContext Result.failure(creditError)
                }
            }

            _routerState.value = _routerState.value.copy(
                isRouting = true,
                currentPersona = persona,
                lastError = null
            )

            try {
                // 2: Safety Check
                val safetyResult: SafetyResult = safetyLayer.validateInput(input)
                if (!safetyResult.isPassed) {
                    val category = safetyResult.category ?: "Safety Violation"
                    val reason = safetyResult.reasonFa ?: "پیام شما با سیاست‌های امنیتی توانا همخوانی ندارد."

                    // Save user message for audit transparency
                    repository.saveMessage(
                        VaultMessageEntity(
                            content = input,
                            sender = MessageSender.USER.name,
                            personaId = persona.id,
                            personaTitle = persona.titleFa,
                            timestamp = System.currentTimeMillis()
                        )
                    )

                    // Save system notice as SYSTEM sender (NOT AI)
                    val systemMsg = VaultMessageEntity(
                        content = "⚠️ [مسدودسازی امنیتی]: $reason",
                        sender = MessageSender.SYSTEM.name,
                        personaId = persona.id,
                        personaTitle = persona.titleFa,
                        timestamp = System.currentTimeMillis(),
                        isError = true
                    )
                    repository.saveMessage(systemMsg)

                    val error = RouterError.SafetyBlocked(category = category, userMessageFa = reason)
                    _routerState.value = _routerState.value.copy(lastError = error)
                    return@withContext Result.failure(error)
                }

                // 3: Save User Message
                val userMessageEntity = VaultMessageEntity(
                    content = input,
                    sender = MessageSender.USER.name,
                    personaId = persona.id,
                    personaTitle = persona.titleFa,
                    timestamp = System.currentTimeMillis()
                )
                repository.saveMessage(userMessageEntity)

                // 4: Determine Real Network State
                val networkStatus = networkMonitor.getCurrentStatus()
                _routerState.value = _routerState.value.copy(networkStatus = networkStatus)

                // 5 & 6: Execute Real Routing & Fallback
                val aiResponseResult = executeRoutingStrategy(input, persona, networkStatus)

                return@withContext if (aiResponseResult.isSuccess) {
                    val aiResponse = aiResponseResult.getOrThrow()

                    // 7: Validate Response Payload
                    val responseText = aiResponse.text.trim()
                    if (responseText.isEmpty()) {
                        val emptyError = RouterError.ProviderError(
                            technicalLog = "Empty text payload after response validation",
                            userMessageFa = "پاسخ دریافتی از هوش مصنوعی خالی بود."
                        )
                        _routerState.value = _routerState.value.copy(lastError = emptyError)
                        return@withContext Result.failure(emptyError)
                    }

                    // Deduct Credits after verified real response
                    accountRepository?.deductCredits(persona.modelTier.creditCost)

                    // 8: Save Verified AI Message (Only real AI response)
                    val aiMessageEntity = VaultMessageEntity(
                        content = responseText,
                        sender = MessageSender.AI.name,
                        personaId = persona.id,
                        personaTitle = persona.titleFa,
                        timestamp = System.currentTimeMillis(),
                        providerUsed = aiResponse.providerName,
                        latencyMs = aiResponse.latencyMs,
                        isError = false
                    )
                    val insertedId = repository.saveMessage(aiMessageEntity)
                    val savedEntity = aiMessageEntity.copy(id = insertedId)

                    // 9: Update State
                    _routerState.value = _routerState.value.copy(
                        activeProviderName = aiResponse.providerName,
                        lastRoutingDurationMs = aiResponse.latencyMs,
                        lastError = null
                    )

                    Result.success(savedEntity)
                } else {
                    val exception = aiResponseResult.exceptionOrNull()
                    val routerError = when (exception) {
                        is RouterError -> exception
                        else -> RouterError.UnknownError(
                            technicalLog = exception?.message ?: "Unknown routing failure",
                            userMessageFa = exception?.localizedMessage ?: "خطا در پردازش پیام"
                        )
                    }

                    // Save structured SYSTEM notification (Sender is SYSTEM, never fake AI)
                    val errorSystemMessage = VaultMessageEntity(
                        content = "❌ ${routerError.userMessageFa}",
                        sender = MessageSender.SYSTEM.name,
                        personaId = persona.id,
                        personaTitle = persona.titleFa,
                        timestamp = System.currentTimeMillis(),
                        providerUsed = _routerState.value.activeProviderName,
                        isError = true
                    )
                    repository.saveMessage(errorSystemMessage)

                    _routerState.value = _routerState.value.copy(lastError = routerError)
                    Result.failure(routerError)
                }
            } catch (e: Exception) {
                Log.e("TavanaRouter", "Exception in processMessage: ${e.javaClass.simpleName}")
                val routerError = if (e is RouterError) e else RouterError.UnknownError(
                    technicalLog = e.message ?: "Unexpected pipeline failure",
                    userMessageFa = e.localizedMessage ?: "خطای پیش‌بینی‌نشده در مسیریاب هوش مصنوعی."
                )

                _routerState.value = _routerState.value.copy(lastError = routerError)
                Result.failure(routerError)
            } finally {
                _routerState.value = _routerState.value.copy(isRouting = false)
            }
        }
    }

    /**
     * Fallback Strategy:
     * ONLINE:
     *   Primary Real Provider (15s timeout) -> on failure -> Fallback Real Provider (10s timeout) -> on failure -> AIUnavailable
     * UNSTABLE:
     *   Primary Real Provider (6s timeout) -> on failure -> Fallback Real Provider (8s timeout) -> on failure -> AIUnavailable
     * OFFLINE:
     *   No network call -> LocalOfflineProvider (returns AIUnavailable without fake text)
     */
    private suspend fun executeRoutingStrategy(
        prompt: String,
        persona: AIPersona,
        networkStatus: NetworkStatus
    ): Result<AIResponse> {
        return when (networkStatus) {
            NetworkStatus.OFFLINE -> {
                Log.i("TavanaRouter", "Network OFFLINE - routing to local offline check (no fake AI)")
                localOfflineProvider.generateResponse(prompt, persona, 1000L)
            }
            NetworkStatus.UNSTABLE -> {
                Log.i("TavanaRouter", "Network UNSTABLE - executing Primary (6000ms timeout) with Fallback")
                val primaryResult = primaryProvider.generateResponse(prompt, persona, 6000L)
                if (primaryResult.isSuccess) {
                    primaryResult
                } else {
                    Log.w("TavanaRouter", "Primary failed in UNSTABLE mode: ${primaryResult.exceptionOrNull()?.message}, executing fallback")
                    val fallbackResult = fallbackProvider.generateResponse(prompt, persona, 8000L)
                    if (fallbackResult.isSuccess) {
                        fallbackResult
                    } else {
                        // If both fail, return structured AIUnavailable or primary error
                        val primaryError = primaryResult.exceptionOrNull()
                        if (primaryError is RouterError) {
                            Result.failure(primaryError)
                        } else {
                            Result.failure(
                                RouterError.AIUnavailable(
                                    technicalLog = "Both primary and fallback providers failed under unstable network conditions.",
                                    userMessageFa = "سرویس هوش مصنوعی در حال حاضر در دسترس نیست."
                                )
                            )
                        }
                    }
                }
            }
            NetworkStatus.ONLINE -> {
                Log.i("TavanaRouter", "Network ONLINE - executing Primary Provider (15000ms timeout)")
                val primaryResult = primaryProvider.generateResponse(prompt, persona, 15000L)
                if (primaryResult.isSuccess) {
                    primaryResult
                } else {
                    Log.w("TavanaRouter", "Primary failed in ONLINE mode: ${primaryResult.exceptionOrNull()?.message}, executing fallback")
                    val fallbackResult = fallbackProvider.generateResponse(prompt, persona, 10000L)
                    if (fallbackResult.isSuccess) {
                        fallbackResult
                    } else {
                        val primaryError = primaryResult.exceptionOrNull()
                        if (primaryError is RouterError) {
                            Result.failure(primaryError)
                        } else {
                            Result.failure(
                                RouterError.AIUnavailable(
                                    technicalLog = "Both primary and fallback providers failed under online network conditions.",
                                    userMessageFa = "سرویس هوش مصنوعی در حال حاضر در دسترس نیست."
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun setPersona(persona: AIPersona) {
        _routerState.value = _routerState.value.copy(currentPersona = persona)
    }

    fun clearError() {
        _routerState.value = _routerState.value.copy(lastError = null)
    }
}
