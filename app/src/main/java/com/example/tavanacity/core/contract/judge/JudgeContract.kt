package com.example.tavanacity.core.contract.judge

import com.squareup.moshi.JsonClass

/**
 * Qualitative scoring metric for architecture evaluation.
 */
@JsonClass(generateAdapter = true)
data class QualityMetricDTO(
    val metricName: String,
    val score: Int, // 0 to 100
    val weight: Double,
    val comments: String
)

/**
 * Platform-Neutral Verdict issued by the Judge.
 */
@JsonClass(generateAdapter = true)
data class JudgeVerdictDTO(
    val isApproved: Boolean,
    val score: Int, // 0 to 100
    val summary: String,
    val feedback: List<String> = emptyList(),
    val metrics: List<QualityMetricDTO> = emptyList(),
    val completedAt: Long = System.currentTimeMillis()
)
