package com.example.tavanacity.domain.architect.model

import java.util.UUID

/**
 * Priority levels for incoming Architect tasks.
 */
enum class TaskPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}

/**
 * Types of tasks supported by TAVANA Master Architect Foundation.
 */
enum class ArchitectTaskType {
    ANALYSIS,
    CODE_MODIFICATION,
    ARCHITECTURE_DESIGN,
    VERIFICATION,
    DIAGNOSTIC,
    CUSTOM
}

/**
 * Representation of an input task sent to the Orchestrator.
 */
data class ArchitectTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val taskType: ArchitectTaskType = ArchitectTaskType.ANALYSIS,
    val priority: TaskPriority = TaskPriority.NORMAL,
    val parameters: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Individual executable step within an ExecutionPlan.
 */
data class ExecutionStep(
    val stepId: String = UUID.randomUUID().toString(),
    val actionType: String,
    val target: String,
    val payload: Map<String, Any> = emptyMap(),
    val timeoutMs: Long = 5000L
)

/**
 * Security level required for executing a plan.
 */
enum class SecurityLevel {
    SAFE,
    RESTRICTED,
    HIGH_RISK
}

/**
 * A structured execution plan prepared by the Orchestrator.
 */
data class ExecutionPlan(
    val planId: String = UUID.randomUUID().toString(),
    val taskId: String,
    val steps: List<ExecutionStep>,
    val estimatedTokenCost: Int = 1,
    val securityLevel: SecurityLevel = SecurityLevel.SAFE,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Result of executing an individual action step.
 */
data class StepExecutionResult(
    val stepId: String,
    val isSuccess: Boolean,
    val output: String,
    val logs: List<String> = emptyList(),
    val durationMs: Long = 0L,
    val error: String? = null
)

/**
 * Aggregate result of executing all steps in an ExecutionPlan.
 */
data class ExecutionResult(
    val planId: String,
    val taskId: String,
    val isSuccess: Boolean,
    val stepResults: List<StepExecutionResult>,
    val totalDurationMs: Long,
    val executionMetadata: Map<String, String> = emptyMap()
)

/**
 * Result returned by the Verification Interface.
 */
data class VerificationResult(
    val isVerified: Boolean,
    val confidenceScore: Double,
    val checksPassed: List<String>,
    val checksFailed: List<String> = emptyList(),
    val details: String
)

/**
 * Final judgment verdict returned by the Judge Interface.
 */
data class JudgeVerdict(
    val isApproved: Boolean,
    val score: Int, // 0 to 100
    val summary: String,
    val feedback: List<String> = emptyList(),
    val completedAt: Long = System.currentTimeMillis()
)

/**
 * Overall Pipeline execution outcome.
 */
data class ArchitectPipelineResult(
    val taskId: String,
    val isSuccessful: Boolean,
    val task: ArchitectTask,
    val plan: ExecutionPlan?,
    val executionResult: ExecutionResult?,
    val verificationResult: VerificationResult?,
    val verdict: JudgeVerdict?,
    val finalOutput: String,
    val totalTimeMs: Long,
    val errorMessage: String? = null
)
