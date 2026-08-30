package com.example.tavanacity.core.contract.verification

import com.squareup.moshi.JsonClass

/**
 * Platform-Neutral verification check detail.
 */
@JsonClass(generateAdapter = true)
data class VerificationCheckDTO(
    val checkName: String,
    val passed: Boolean,
    val description: String
)

/**
 * Platform-Neutral Verification Result emitted by Verification Engine.
 */
@JsonClass(generateAdapter = true)
data class VerificationResultDTO(
    val isVerified: Boolean,
    val confidenceScore: Double,
    val checksPassed: List<String>,
    val checksFailed: List<String> = emptyList(),
    val details: String,
    val checks: List<VerificationCheckDTO> = emptyList(),
    val verifiedAt: Long = System.currentTimeMillis()
)
