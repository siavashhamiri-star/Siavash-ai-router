package com.example.tavanacity.domain.model

enum class AuthType {
    ANONYMOUS_DEVICE,
    PHONE_OTP,
    EMAIL_VERIFIED
}

/**
 * Authoritative User Account structure.
 * Independent of physical device to allow cross-device sync and purchase restoration.
 */
data class UserAccount(
    val userId: String,
    val identifier: String = "کاربر مهمان",
    val authType: AuthType = AuthType.ANONYMOUS_DEVICE,
    val plan: UserPlan = UserPlan.FREE,
    val entitlementStatus: EntitlementStatus = EntitlementStatus.FREE,
    val subscriptionStart: Long? = null,
    val subscriptionEnd: Long? = null,
    val creditBalance: Int = UserPlan.FREE.dailyCredits,
    val purchaseProvider: String? = null,
    val purchaseId: String? = null,
    val purchaseToken: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isSubscriptionActive: Boolean
        get() {
            if (plan == UserPlan.FREE) return true
            if (entitlementStatus != EntitlementStatus.ACTIVE && entitlementStatus != EntitlementStatus.GRACE_PERIOD) return false
            val end = subscriptionEnd ?: return false
            return System.currentTimeMillis() <= end
        }

    val isAuthenticated: Boolean
        get() = authType != AuthType.ANONYMOUS_DEVICE

    val phoneNumber: String?
        get() = if (authType == AuthType.PHONE_OTP) identifier else null

    val email: String?
        get() = if (authType == AuthType.EMAIL_VERIFIED) identifier else null

    fun hasSufficientCredits(requiredCredits: Int): Boolean {
        return creditBalance >= requiredCredits
    }

    companion object {
        fun createDefault(userId: String = "guest_${System.currentTimeMillis().toString().takeLast(6)}"): UserAccount {
            return UserAccount(
                userId = userId,
                identifier = "کاربر مهمان (دستگاه)",
                authType = AuthType.ANONYMOUS_DEVICE,
                plan = UserPlan.FREE,
                entitlementStatus = EntitlementStatus.FREE,
                creditBalance = UserPlan.FREE.dailyCredits
            )
        }
    }
}
