package com.example.tavanacity.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tavanacity.core.accessibility.TavanaTextToSpeech
import com.example.tavanacity.data.billing.CreditPackage
import com.example.tavanacity.data.local.ChatVaultDatabase
import com.example.tavanacity.data.local.VaultMessageEntity
import com.example.tavanacity.data.network.NetworkMonitor
import com.example.tavanacity.data.provider.CloudAIProviderAdapter
import com.example.tavanacity.data.provider.FallbackAIProviderAdapter
import com.example.tavanacity.data.provider.LocalOfflineProviderAdapter
import com.example.tavanacity.data.repository.AccountRepository
import com.example.tavanacity.data.repository.ChatVaultRepository
import com.example.tavanacity.data.safety.TavanaCitySafetyLayer
import com.example.tavanacity.domain.model.AIPersona
import com.example.tavanacity.domain.model.AccessibilityConfig
import com.example.tavanacity.domain.model.NetworkStatus
import com.example.tavanacity.domain.model.RouterError
import com.example.tavanacity.domain.model.UserAccount
import com.example.tavanacity.domain.model.UserPlan
import com.example.tavanacity.domain.router.RouterState
import com.example.tavanacity.domain.router.TavanaCityAIRouter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiEvent {
    data class ShowToast(val message: String) : UiEvent
    data class CopyToClipboard(val text: String) : UiEvent
}

class RouterViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ChatVaultDatabase.getInstance(application)
    private val repository = ChatVaultRepository(database.messageDao())
    val networkMonitor = NetworkMonitor(application, viewModelScope)

    val accountRepository = AccountRepository()

    private val safetyLayer = TavanaCitySafetyLayer()
    private val cloudProvider = CloudAIProviderAdapter()
    private val fallbackProvider = FallbackAIProviderAdapter(cloudProvider)
    private val localOfflineProvider = LocalOfflineProviderAdapter()

    val router = TavanaCityAIRouter(
        repository = repository,
        networkMonitor = networkMonitor,
        safetyLayer = safetyLayer,
        primaryProvider = cloudProvider,
        fallbackProvider = fallbackProvider,
        localOfflineProvider = localOfflineProvider,
        accountRepository = accountRepository
    )

    val messages: StateFlow<List<VaultMessageEntity>> = repository.messagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routerState: StateFlow<RouterState> = router.routerState
    val networkStatus: StateFlow<NetworkStatus> = networkMonitor.networkStatus
    val userAccount: StateFlow<UserAccount> = accountRepository.accountState

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    // Dialog & Sheet states
    private val _reportingMessage = MutableStateFlow<VaultMessageEntity?>(null)
    val reportingMessage: StateFlow<VaultMessageEntity?> = _reportingMessage.asStateFlow()

    private val _showClearDialog = MutableStateFlow(false)
    val showClearDialog: StateFlow<Boolean> = _showClearDialog.asStateFlow()

    private val _showSubscriptionSheet = MutableStateFlow(false)
    val showSubscriptionSheet: StateFlow<Boolean> = _showSubscriptionSheet.asStateFlow()

    private val _showAccountDialog = MutableStateFlow(false)
    val showAccountDialog: StateFlow<Boolean> = _showAccountDialog.asStateFlow()

    private val _isPurchasing = MutableStateFlow(false)
    val isPurchasing: StateFlow<Boolean> = _isPurchasing.asStateFlow()

    // Accessibility & Calm Mode configuration state
    private val _accessibilityConfig = MutableStateFlow(AccessibilityConfig())
    val accessibilityConfig: StateFlow<AccessibilityConfig> = _accessibilityConfig.asStateFlow()

    private val _showAccessibilityDialog = MutableStateFlow(false)
    val showAccessibilityDialog: StateFlow<Boolean> = _showAccessibilityDialog.asStateFlow()

    private val tts = TavanaTextToSpeech(application)
    private val _currentlySpeakingMessageId = MutableStateFlow<Long?>(null)
    val currentlySpeakingMessageId: StateFlow<Long?> = _currentlySpeakingMessageId.asStateFlow()

    fun toggleLargeFont() {
        val current = _accessibilityConfig.value
        val newScale = if (!current.isLargeFontEnabled) 1.25f else 1.0f
        _accessibilityConfig.value = current.copy(
            isLargeFontEnabled = !current.isLargeFontEnabled,
            fontScaleFactor = newScale
        )
    }

    fun toggleHighContrast() {
        val current = _accessibilityConfig.value
        _accessibilityConfig.value = current.copy(
            isHighContrastEnabled = !current.isHighContrastEnabled
        )
    }

    fun toggleCalmMode() {
        val current = _accessibilityConfig.value
        _accessibilityConfig.value = current.copy(
            isCalmModeEnabled = !current.isCalmModeEnabled
        )
    }

    fun openAccessibilityDialog() {
        _showAccessibilityDialog.value = true
    }

    fun closeAccessibilityDialog() {
        _showAccessibilityDialog.value = false
    }

    fun speakMessage(message: VaultMessageEntity) {
        if (_currentlySpeakingMessageId.value == message.id && tts.isSpeaking()) {
            tts.stop()
            _currentlySpeakingMessageId.value = null
        } else {
            tts.speak(message.content)
            _currentlySpeakingMessageId.value = message.id
        }
    }

    fun stopSpeaking() {
        tts.stop()
        _currentlySpeakingMessageId.value = null
    }

    fun onInputChanged(text: String) {
        _inputText.value = text
    }

    fun selectPersona(persona: AIPersona) {
        // Pre-check if persona requires plan upgrade
        if (!accountRepository.canAccessPersona(persona)) {
            val neededPlan = UserPlan.entries.firstOrNull { it.canAccessModelTier(persona.modelTier) } ?: UserPlan.PRO
            viewModelScope.launch {
                _uiEvents.emit(UiEvent.ShowToast("برای استفاده از ${persona.titleFa} به اشتراک ${neededPlan.titleFa} نیاز دارید."))
            }
            _showSubscriptionSheet.value = true
            return
        }
        router.setPersona(persona)
    }

    fun sendMessage() {
        val currentInput = _inputText.value.trim()
        if (currentInput.isBlank()) return

        _inputText.value = ""
        viewModelScope.launch {
            val result = router.processMessage(currentInput)
            if (result.isFailure) {
                val err = result.exceptionOrNull()
                if (err is RouterError.InsufficientCredit || err is RouterError.EntitlementRequired) {
                    _showSubscriptionSheet.value = true
                }
            }
        }
    }

    fun retryMessage(content: String) {
        viewModelScope.launch {
            val result = router.processMessage(content)
            if (result.isFailure) {
                val err = result.exceptionOrNull()
                if (err is RouterError.InsufficientCredit || err is RouterError.EntitlementRequired) {
                    _showSubscriptionSheet.value = true
                }
            }
        }
    }

    fun openSubscriptionSheet() {
        _showSubscriptionSheet.value = true
    }

    fun closeSubscriptionSheet() {
        _showSubscriptionSheet.value = false
    }

    fun openAccountDialog() {
        _showAccountDialog.value = true
    }

    fun closeAccountDialog() {
        _showAccountDialog.value = false
    }

    fun purchasePlan(context: Context, plan: UserPlan) {
        viewModelScope.launch {
            _isPurchasing.value = true
            try {
                val result = accountRepository.purchasePlan(context, plan)
                if (result.isSuccess) {
                    val updated = result.getOrThrow()
                    _uiEvents.emit(UiEvent.ShowToast("اشتراک ${updated.plan.titleFa} با موفقیت در مایکت فعال شد!"))
                    _showSubscriptionSheet.value = false
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "خطا در تایید خرید"
                    _uiEvents.emit(UiEvent.ShowToast("خطا در خرید: $errorMsg"))
                }
            } finally {
                _isPurchasing.value = false
            }
        }
    }

    fun purchaseCreditPack(context: Context, pack: CreditPackage) {
        viewModelScope.launch {
            _isPurchasing.value = true
            try {
                val result = accountRepository.purchaseCreditPack(context, pack)
                if (result.isSuccess) {
                    val updated = result.getOrThrow()
                    _uiEvents.emit(UiEvent.ShowToast("${pack.titleFa} فعال شد! موجودی فعلی: ${updated.creditBalance} سکه"))
                    _showSubscriptionSheet.value = false
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "خطا در خرید بسته اعتباری"
                    _uiEvents.emit(UiEvent.ShowToast("خطا در خرید: $errorMsg"))
                }
            } finally {
                _isPurchasing.value = false
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            _isPurchasing.value = true
            try {
                val result = accountRepository.restorePurchases()
                if (result.isSuccess) {
                    val updated = result.getOrThrow()
                    if (updated.plan != UserPlan.FREE) {
                        _uiEvents.emit(UiEvent.ShowToast("اشتراک ${updated.plan.titleFa} با موفقیت بازیابی شد."))
                    } else {
                        _uiEvents.emit(UiEvent.ShowToast("خریدی برای این حساب در مایکت یافت نشد."))
                    }
                } else {
                    _uiEvents.emit(UiEvent.ShowToast("خطا در بازیابی خریدهای مایکت."))
                }
            } finally {
                _isPurchasing.value = false
            }
        }
    }

    fun loginPhone(phone: String, otp: String) {
        viewModelScope.launch {
            val result = accountRepository.loginWithPhone(phone, otp)
            if (result.isSuccess) {
                _uiEvents.emit(UiEvent.ShowToast("ورود با شماره $phone با موفقیت انجام شد."))
                _showAccountDialog.value = false
            } else {
                _uiEvents.emit(UiEvent.ShowToast(result.exceptionOrNull()?.message ?: "خطا در ورود"))
            }
        }
    }

    fun loginEmail(email: String, code: String) {
        viewModelScope.launch {
            val result = accountRepository.loginWithEmail(email, code)
            if (result.isSuccess) {
                _uiEvents.emit(UiEvent.ShowToast("ورود با ایمیل $email با موفقیت انجام شد."))
                _showAccountDialog.value = false
            } else {
                _uiEvents.emit(UiEvent.ShowToast(result.exceptionOrNull()?.message ?: "خطا در ورود"))
            }
        }
    }

    fun openReportDialog(message: VaultMessageEntity) {
        _reportingMessage.value = message
    }

    fun closeReportDialog() {
        _reportingMessage.value = null
    }

    fun submitReport(reason: String) {
        val message = _reportingMessage.value ?: return
        viewModelScope.launch {
            repository.reportMessage(message.id, reason)
            _reportingMessage.value = null
            _uiEvents.emit(UiEvent.ShowToast("گزارش پیام با موفقیت در سیستم ثبت گردید."))
        }
    }

    fun copyMessageText(text: String) {
        viewModelScope.launch {
            _uiEvents.emit(UiEvent.CopyToClipboard(text))
            _uiEvents.emit(UiEvent.ShowToast("متن پیام در حافظه موقت کپی شد"))
        }
    }

    fun openClearDialog() {
        _showClearDialog.value = true
    }

    fun closeClearDialog() {
        _showClearDialog.value = false
    }

    fun confirmClearVault() {
        viewModelScope.launch {
            repository.clearVault()
            _showClearDialog.value = false
            _uiEvents.emit(UiEvent.ShowToast("تاریخچه پیام‌های مخزن گفتگو پاک‌سازی شد"))
        }
    }

    fun dismissError() {
        router.clearError()
    }

    override fun onCleared() {
        super.onCleared()
        tts.shutdown()
        networkMonitor.unregister()
    }
}

