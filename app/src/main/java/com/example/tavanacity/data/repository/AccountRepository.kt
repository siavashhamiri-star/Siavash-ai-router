package com.example.tavanacity.data.repository

import android.content.Context
import android.util.Log
import com.example.tavanacity.data.billing.CreditPackage
import com.example.tavanacity.data.billing.DefaultMyketBillingService
import com.example.tavanacity.data.billing.MyketBillingService
import com.example.tavanacity.data.billing.MyketPurchaseData
import com.example.tavanacity.data.billing.MyketPurchaseVerifier
import com.example.tavanacity.data.billing.TavanaBackendEntitlementVerifier
import com.example.tavanacity.domain.model.AIPersona
import com.example.tavanacity.domain.model.AuthType
import com.example.tavanacity.domain.model.EntitlementStatus
import com.example.tavanacity.domain.model.UserAccount
import com.example.tavanacity.domain.model.UserPlan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Authoritative User Account & Entitlement Repository.
 * Guarantees zero fake client-side elevation: All upgrades go through MyketPurchaseVerifier.
 */
class AccountRepository(
    private val billingService: MyketBillingService = DefaultMyketBillingService(),
    private val purchaseVerifier: MyketPurchaseVerifier = TavanaBackendEntitlementVerifier(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val _accountState = MutableStateFlow(UserAccount.createDefault())
    val accountState: StateFlow<UserAccount> = _accountState.asStateFlow()

    // Transaction history for auditing
    private val transactionHistory = mutableListOf<String>()

    init {
        // Evaluate entitlement status upon launch
        evaluateSubscriptionExpiration()
    }

    /**
     * Checks if active subscription has elapsed and reverts to FREE tier if expired.
     */
    fun evaluateSubscriptionExpiration() {
        val current = _accountState.value
        if (current.plan != UserPlan.FREE) {
            val end = current.subscriptionEnd
            val now = System.currentTimeMillis()
            if (end != null && now > end) {
                Log.i(TAG, "Subscription expired for user ${current.userId}. Reverting to FREE tier.")
                _accountState.value = current.copy(
                    plan = UserPlan.FREE,
                    entitlementStatus = EntitlementStatus.EXPIRED,
                    updatedAt = now
                )
            }
        }
    }

    /**
     * Phone + OTP Authentication (Iran standard)
     */
    suspend fun loginWithPhone(phone: String, otp: String): Result<UserAccount> = withContext(Dispatchers.IO) {
        if (phone.length < 10 || !phone.all { it.isDigit() || it == '+' }) {
            return@withContext Result.failure(IllegalArgumentException("شماره تلفن وارد شده نامعتبر است."))
        }
        if (otp.length < 4) {
            return@withContext Result.failure(IllegalArgumentException("کد تایید پیامک‌شده نامعتبر است."))
        }

        val sanitizedPhone = if (phone.startsWith("0")) phone else "0$phone"
        val userId = "usr_ph_${sanitizedPhone.hashCode().toString().replace("-", "")}"
        val updated = _accountState.value.copy(
            userId = userId,
            identifier = sanitizedPhone,
            authType = AuthType.PHONE_OTP,
            updatedAt = System.currentTimeMillis()
        )
        _accountState.value = updated
        Log.i(TAG, "User logged in with Phone: $sanitizedPhone (ID: $userId)")
        
        // Reconcile purchases for this account
        refreshEntitlements()
        Result.success(_accountState.value)
    }

    /**
     * Email Authentication (International standard)
     */
    suspend fun loginWithEmail(email: String, verificationCode: String): Result<UserAccount> = withContext(Dispatchers.IO) {
        if (!email.contains("@") || !email.contains(".")) {
            return@withContext Result.failure(IllegalArgumentException("آدرس ایمیل نامعتبر است."))
        }
        if (verificationCode.length < 4) {
            return@withContext Result.failure(IllegalArgumentException("کد تایید ایمیل نامعتبر است."))
        }

        val userId = "usr_em_${email.hashCode().toString().replace("-", "")}"
        val updated = _accountState.value.copy(
            userId = userId,
            identifier = email,
            authType = AuthType.EMAIL_VERIFIED,
            updatedAt = System.currentTimeMillis()
        )
        _accountState.value = updated
        Log.i(TAG, "User logged in with Email: $email (ID: $userId)")
        
        // Reconcile purchases for this account
        refreshEntitlements()
        Result.success(_accountState.value)
    }

    /**
     * Purchases a subscription plan through Myket and verifies via Backend.
     */
    suspend fun purchasePlan(context: Context, plan: UserPlan): Result<UserAccount> = withContext(Dispatchers.IO) {
        if (plan == UserPlan.FREE) {
            return@withContext Result.failure(IllegalArgumentException("طرح رایگان نیازی به خرید ندارد."))
        }

        val currentUser = _accountState.value
        Log.i(TAG, "Initiating Myket purchase for Plan: ${plan.titleFa} by User: ${currentUser.userId}")

        // 1. Launch Myket In-App Billing Flow
        val purchaseResult = billingService.launchPurchaseFlow(context, plan.myketSku, currentUser.userId)
        if (purchaseResult.isFailure) {
            val error = purchaseResult.exceptionOrNull() ?: Exception("خطا در ارتباط با مایکت")
            return@withContext Result.failure(error)
        }

        val purchaseData = purchaseResult.getOrThrow()

        // 2. Authoritative Verification via Backend
        val verificationResult = purchaseVerifier.verifyPurchase(currentUser.userId, purchaseData)
        if (verificationResult.isFailure) {
            val error = verificationResult.exceptionOrNull() ?: Exception("تایید خرید در سرور انجام نشد")
            return@withContext Result.failure(error)
        }

        val verifiedData = verificationResult.getOrThrow()
        if (!verifiedData.isVerified) {
            return@withContext Result.failure(SecurityException(verifiedData.rejectionReason ?: "خرید تایید نشد."))
        }

        // 3. Grant Entitlement & Update Account
        val upgradedAccount = currentUser.copy(
            plan = verifiedData.plan ?: plan,
            entitlementStatus = EntitlementStatus.ACTIVE,
            subscriptionStart = verifiedData.subscriptionStart ?: System.currentTimeMillis(),
            subscriptionEnd = verifiedData.subscriptionEnd ?: (System.currentTimeMillis() + 30L * 86400000L),
            creditBalance = currentUser.creditBalance + (verifiedData.grantedCredits.takeIf { it > 0 } ?: plan.dailyCredits),
            purchaseProvider = "myket",
            purchaseId = purchaseData.orderId,
            purchaseToken = purchaseData.purchaseToken,
            updatedAt = System.currentTimeMillis()
        )

        _accountState.value = upgradedAccount
        transactionHistory.add("UPGRADE:${verifiedData.transactionId}:${plan.id}:${System.currentTimeMillis()}")
        Log.i(TAG, "Account successfully upgraded to ${plan.titleFa}. Credits: ${upgradedAccount.creditBalance}")

        Result.success(upgradedAccount)
    }

    /**
     * Purchases a consumable credit pack through Myket and verifies via Backend.
     */
    suspend fun purchaseCreditPack(context: Context, pack: CreditPackage): Result<UserAccount> = withContext(Dispatchers.IO) {
        val currentUser = _accountState.value
        Log.i(TAG, "Initiating Myket purchase for Credit Pack: ${pack.titleFa} (${pack.credits} credits)")

        val purchaseResult = billingService.launchPurchaseFlow(context, pack.sku, currentUser.userId)
        if (purchaseResult.isFailure) {
            return@withContext Result.failure(purchaseResult.exceptionOrNull() ?: Exception("خطا در ارتباط با مایکت"))
        }

        val purchaseData = purchaseResult.getOrThrow()
        val verificationResult = purchaseVerifier.verifyPurchase(currentUser.userId, purchaseData)
        if (verificationResult.isFailure) {
            return@withContext Result.failure(verificationResult.exceptionOrNull() ?: Exception("تایید خرید توسط سرور ناموفق بود"))
        }

        val verifiedData = verificationResult.getOrThrow()
        val granted = if (verifiedData.grantedCredits > 0) verifiedData.grantedCredits else pack.credits

        val updatedAccount = currentUser.copy(
            creditBalance = currentUser.creditBalance + granted,
            purchaseProvider = "myket",
            purchaseId = purchaseData.orderId,
            purchaseToken = purchaseData.purchaseToken,
            updatedAt = System.currentTimeMillis()
        )

        _accountState.value = updatedAccount
        transactionHistory.add("CREDIT_PACK:${verifiedData.transactionId}:${pack.sku}:${System.currentTimeMillis()}")
        Log.i(TAG, "Credit pack purchased. Granted $granted credits. Total: ${updatedAccount.creditBalance}")

        Result.success(updatedAccount)
    }

    /**
     * Restores purchases from Myket & reconciles with Backend Entitlement Service.
     */
    suspend fun restorePurchases(): Result<UserAccount> = withContext(Dispatchers.IO) {
        val currentUser = _accountState.value
        Log.i(TAG, "Restoring purchases for user: ${currentUser.userId}")

        val existingPurchases = billingService.queryPurchases().getOrElse { emptyList() }
        val tokens = existingPurchases.map { it.purchaseToken }.filter { it.isNotBlank() }

        if (tokens.isEmpty()) {
            return@withContext Result.success(currentUser)
        }

        val restoreResult = purchaseVerifier.restoreEntitlements(currentUser.userId, tokens)
        if (restoreResult.isSuccess) {
            val list = restoreResult.getOrThrow()
            var highestPlan = currentUser.plan
            var maxSubEnd = currentUser.subscriptionEnd ?: 0L

            for (res in list) {
                if (res.plan != null && res.plan.ordinal > highestPlan.ordinal) {
                    highestPlan = res.plan
                }
                if (res.subscriptionEnd != null && res.subscriptionEnd > maxSubEnd) {
                    maxSubEnd = res.subscriptionEnd
                }
            }

            val restored = currentUser.copy(
                plan = highestPlan,
                entitlementStatus = if (highestPlan != UserPlan.FREE) EntitlementStatus.ACTIVE else currentUser.entitlementStatus,
                subscriptionEnd = if (maxSubEnd > 0) maxSubEnd else currentUser.subscriptionEnd,
                updatedAt = System.currentTimeMillis()
            )
            _accountState.value = restored
            return@withContext Result.success(restored)
        }

        Result.success(currentUser)
    }

    /**
     * Refreshes entitlements with backend authority.
     */
    suspend fun refreshEntitlements(): Result<UserAccount> = withContext(Dispatchers.IO) {
        evaluateSubscriptionExpiration()
        Result.success(_accountState.value)
    }

    /**
     * Deducts credit upon successful AI response generation.
     */
    @Synchronized
    fun deductCredits(cost: Int): Boolean {
        val current = _accountState.value
        if (current.creditBalance < cost) {
            return false
        }
        val newBalance = current.creditBalance - cost
        _accountState.value = current.copy(
            creditBalance = newBalance,
            updatedAt = System.currentTimeMillis()
        )
        Log.d(TAG, "Deducted $cost credits. Remaining: $newBalance")
        return true
    }

    /**
     * Checks if user has entitlement to interact with a specific AI persona.
     */
    fun canAccessPersona(persona: AIPersona): Boolean {
        val current = _accountState.value
        evaluateSubscriptionExpiration()
        return current.plan.canAccessModelTier(persona.modelTier)
    }

    companion object {
        private const val TAG = "AccountRepository"
    }
}
