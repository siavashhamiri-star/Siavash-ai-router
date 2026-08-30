package com.example.tavanacity.core.contract.governor

import com.example.tavanacity.core.contract.execution.SecurityLevelDTO
import com.squareup.moshi.JsonClass

/**
 * Governor decision taxonomy.
 */
@JsonClass(generateAdapter = false)
enum class GovernorDecisionDTO {
    ALLOWED,
    BLOCKED,
    CONDITIONAL_APPROVAL
}

/**
 * Structured Governor policy evaluation.
 */
@JsonClass(generateAdapter = true)
data class GovernorEvaluationDTO(
    val decision: GovernorDecisionDTO,
    val reason: String,
    val securityLevel: SecurityLevelDTO,
    val requiredModifications: List<String> = emptyList(),
    val policyViolations: List<String> = emptyList(),
    val evaluatedAt: Long = System.currentTimeMillis()
)

/**
 * Request requiring human or elevated supervisor approval.
 */
@JsonClass(generateAdapter = true)
data class ApprovalRequestDTO(
    val requestId: String,
    val taskId: String,
    val planId: String,
    val riskSummary: String,
    val securityLevel: SecurityLevelDTO,
    val requestedAt: Long = System.currentTimeMillis(),
    val isApproved: Boolean? = null,
    val approvedBy: String? = null,
    val decisionNote: String? = null
)
