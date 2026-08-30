package com.example.tavanacity.data.billing

/**
 * Standard In-App Purchase data format returned by Myket Billing API.
 */
data class MyketPurchaseData(
    val orderId: String,
    val packageName: String,
    val productId: String, // SKU (e.g. tavana_plus_monthly, tavana_pro_monthly)
    val purchaseTime: Long,
    val purchaseState: Int = 0, // 0: PURCHASED, 1: CANCELED, 2: REFUNDED
    val purchaseToken: String,
    val developerPayload: String? = null,
    val isAutoRenewing: Boolean = false,
    val originalJson: String? = null,
    val signature: String? = null
)
