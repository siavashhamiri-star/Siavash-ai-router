package com.example.tavanacity.data.billing

/**
 * Interface contract for validating Myket In-App Purchases against backend entitlement authority.
 * Strictly adheres to rule: No client-side local spoofing. Backend is the single source of truth.
 */
interface MyketPurchaseVerifier {
    /**
     * Verifies purchase with TAVANA backend / Myket Developer API.
     * Prevents token reuse, checks signature, and generates/extends entitlement.
     */
    suspend fun verifyPurchase(
        userId: String,
        purchaseData: MyketPurchaseData
    ): Result<PurchaseVerificationResult>

    /**
     * Restores and reconciles past purchases from backend authority.
     */
    suspend fun restoreEntitlements(
        userId: String,
        activeTokens: List<String>
    ): Result<List<PurchaseVerificationResult>>
}
