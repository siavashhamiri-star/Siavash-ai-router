package com.example.tavanacity.data.billing

import com.example.tavanacity.domain.model.UserPlan

/**
 * Result returned from authoritative TAVANA Backend Myket Verification API.
 */
data class PurchaseVerificationResult(
    val isVerified: Boolean,
    val transactionId: String,
    val plan: UserPlan? = null,
    val grantedCredits: Int = 0,
    val subscriptionStart: Long? = null,
    val subscriptionEnd: Long? = null,
    val purchaseToken: String,
    val isConsumable: Boolean = false,
    val rejectionReason: String? = null,
    val serverTimestamp: Long = System.currentTimeMillis()
)
