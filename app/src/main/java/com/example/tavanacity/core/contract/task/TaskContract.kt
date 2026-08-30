package com.example.tavanacity.core.contract.task

import com.squareup.moshi.JsonClass

/**
 * Platform-Neutral Task Priority enumeration.
 */
@JsonClass(generateAdapter = false)
enum class TaskPriorityDTO {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}

/**
 * Platform-Neutral Task Type taxonomy.
 */
@JsonClass(generateAdapter = false)
enum class ArchitectTaskTypeDTO {
    ANALYSIS,
    CODE_MODIFICATION,
    ARCHITECTURE_DESIGN,
    VERIFICATION,
    DIAGNOSTIC,
    CUSTOM
}

/**
 * Platform-Neutral Lifecycle Status for a Task.
 */
@JsonClass(generateAdapter = false)
enum class TaskStatusDTO {
    SUBMITTED,
    QUEUED,
    PLANNING,
    GOVERNANCE_REVIEW,
    EXECUTING,
    VERIFYING,
    JUDGING,
    COMPLETED,
    FAILED,
    REJECTED_BY_POLICY
}

/**
 * Platform-Neutral Request object for submitting a new task to TAVANA Core.
 */
@JsonClass(generateAdapter = true)
data class TaskRequestDTO(
    val title: String,
    val description: String,
    val taskType: ArchitectTaskTypeDTO = ArchitectTaskTypeDTO.ANALYSIS,
    val priority: TaskPriorityDTO = TaskPriorityDTO.NORMAL,
    val parameters: Map<String, String> = emptyMap(),
    val clientId: String = "unknown-client",
    val clientPlatform: String = "GENERIC" // "ANDROID", "PWA_WEB", "CLI"
)

/**
 * Platform-Neutral Representation of a Task in TAVANA Core.
 */
@JsonClass(generateAdapter = true)
data class TaskDTO(
    val id: String,
    val title: String,
    val description: String,
    val taskType: ArchitectTaskTypeDTO,
    val priority: TaskPriorityDTO,
    val status: TaskStatusDTO = TaskStatusDTO.SUBMITTED,
    val parameters: Map<String, String> = emptyMap(),
    val clientId: String,
    val clientPlatform: String,
    val createdAt: Long,
    val completedAt: Long? = null
)
