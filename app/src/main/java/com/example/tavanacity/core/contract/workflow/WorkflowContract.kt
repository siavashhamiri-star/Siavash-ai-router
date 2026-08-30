package com.example.tavanacity.core.contract.workflow

import com.squareup.moshi.JsonClass

/**
 * Platform-Neutral Workflow States shared across Android, Web/PWA, and Backend.
 */
@JsonClass(generateAdapter = false)
enum class WorkflowStateDTO {
    IDLE,
    TASK_RECEIVED,
    PLANNING,
    GOVERNANCE_CHECK,
    READY_FOR_EXECUTION,
    EXECUTING,
    VERIFYING,
    JUDGING,
    COMPLETED,
    FAILED,
    BLOCKED_BY_GOVERNOR
}

/**
 * Platform-Neutral Workflow State Snapshot for real-time telemetry / sync.
 */
@JsonClass(generateAdapter = true)
data class WorkflowSnapshotDTO(
    val state: WorkflowStateDTO,
    val currentTaskId: String? = null,
    val currentPlanId: String? = null,
    val statusMessage: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Event for state transitions broadcasted over WebSocket or Server-Sent Events.
 */
@JsonClass(generateAdapter = true)
data class WorkflowEventDTO(
    val eventId: String,
    val taskId: String,
    val fromState: WorkflowStateDTO,
    val toState: WorkflowStateDTO,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
