package com.example.tavanacity.core.contract.mapper

import com.example.tavanacity.core.contract.execution.ExecutionPlanDTO
import com.example.tavanacity.core.contract.execution.ExecutionResultDTO
import com.example.tavanacity.core.contract.execution.ExecutionStepDTO
import com.example.tavanacity.core.contract.execution.SecurityLevelDTO
import com.example.tavanacity.core.contract.execution.StepExecutionResultDTO
import com.example.tavanacity.core.contract.governor.GovernorDecisionDTO
import com.example.tavanacity.core.contract.governor.GovernorEvaluationDTO
import com.example.tavanacity.core.contract.judge.JudgeVerdictDTO
import com.example.tavanacity.core.contract.memory.MemoryRecordDTO
import com.example.tavanacity.core.contract.task.ArchitectTaskTypeDTO
import com.example.tavanacity.core.contract.task.TaskDTO
import com.example.tavanacity.core.contract.task.TaskPriorityDTO
import com.example.tavanacity.core.contract.task.TaskRequestDTO
import com.example.tavanacity.core.contract.task.TaskStatusDTO
import com.example.tavanacity.core.contract.verification.VerificationResultDTO
import com.example.tavanacity.core.contract.workflow.WorkflowSnapshotDTO
import com.example.tavanacity.core.contract.workflow.WorkflowStateDTO
import com.example.tavanacity.domain.architect.governor.GovernorDecision
import com.example.tavanacity.domain.architect.governor.GovernorEvaluation
import com.example.tavanacity.domain.architect.model.JudgeVerdict
import com.example.tavanacity.domain.architect.memory.MemoryEntry
import com.example.tavanacity.domain.architect.model.ArchitectTask
import com.example.tavanacity.domain.architect.model.ArchitectTaskType
import com.example.tavanacity.domain.architect.model.ExecutionPlan
import com.example.tavanacity.domain.architect.model.ExecutionResult
import com.example.tavanacity.domain.architect.model.ExecutionStep
import com.example.tavanacity.domain.architect.model.SecurityLevel
import com.example.tavanacity.domain.architect.model.StepExecutionResult
import com.example.tavanacity.domain.architect.model.TaskPriority
import com.example.tavanacity.domain.architect.model.VerificationResult
import com.example.tavanacity.domain.architect.state.WorkflowState
import com.example.tavanacity.domain.architect.state.WorkflowStateSnapshot

/**
 * Clean bidirectional mappers between Android Domain Models and Platform-Neutral Core Contract DTOs.
 */
object ContractMappers {

    fun toDTO(task: ArchitectTask, status: TaskStatusDTO = TaskStatusDTO.SUBMITTED): TaskDTO = TaskDTO(
        id = task.id,
        title = task.title,
        description = task.description,
        taskType = toDTO(task.taskType),
        priority = toDTO(task.priority),
        status = status,
        parameters = task.parameters,
        clientId = "android-client",
        clientPlatform = "ANDROID",
        createdAt = task.createdAt
    )

    fun toDomain(request: TaskRequestDTO, id: String = java.util.UUID.randomUUID().toString()): ArchitectTask = ArchitectTask(
        id = id,
        title = request.title,
        description = request.description,
        taskType = toDomain(request.taskType),
        priority = toDomain(request.priority),
        parameters = request.parameters,
        createdAt = System.currentTimeMillis()
    )

    fun toDTO(taskType: ArchitectTaskType): ArchitectTaskTypeDTO = when (taskType) {
        ArchitectTaskType.ANALYSIS -> ArchitectTaskTypeDTO.ANALYSIS
        ArchitectTaskType.CODE_MODIFICATION -> ArchitectTaskTypeDTO.CODE_MODIFICATION
        ArchitectTaskType.ARCHITECTURE_DESIGN -> ArchitectTaskTypeDTO.ARCHITECTURE_DESIGN
        ArchitectTaskType.VERIFICATION -> ArchitectTaskTypeDTO.VERIFICATION
        ArchitectTaskType.DIAGNOSTIC -> ArchitectTaskTypeDTO.DIAGNOSTIC
        ArchitectTaskType.CUSTOM -> ArchitectTaskTypeDTO.CUSTOM
    }

    fun toDomain(typeDTO: ArchitectTaskTypeDTO): ArchitectTaskType = when (typeDTO) {
        ArchitectTaskTypeDTO.ANALYSIS -> ArchitectTaskType.ANALYSIS
        ArchitectTaskTypeDTO.CODE_MODIFICATION -> ArchitectTaskType.CODE_MODIFICATION
        ArchitectTaskTypeDTO.ARCHITECTURE_DESIGN -> ArchitectTaskType.ARCHITECTURE_DESIGN
        ArchitectTaskTypeDTO.VERIFICATION -> ArchitectTaskType.VERIFICATION
        ArchitectTaskTypeDTO.DIAGNOSTIC -> ArchitectTaskType.DIAGNOSTIC
        ArchitectTaskTypeDTO.CUSTOM -> ArchitectTaskType.CUSTOM
    }

    fun toDTO(priority: TaskPriority): TaskPriorityDTO = when (priority) {
        TaskPriority.LOW -> TaskPriorityDTO.LOW
        TaskPriority.NORMAL -> TaskPriorityDTO.NORMAL
        TaskPriority.HIGH -> TaskPriorityDTO.HIGH
        TaskPriority.CRITICAL -> TaskPriorityDTO.CRITICAL
    }

    fun toDomain(dto: TaskPriorityDTO): TaskPriority = when (dto) {
        TaskPriorityDTO.LOW -> TaskPriority.LOW
        TaskPriorityDTO.NORMAL -> TaskPriority.NORMAL
        TaskPriorityDTO.HIGH -> TaskPriority.HIGH
        TaskPriorityDTO.CRITICAL -> TaskPriority.CRITICAL
    }

    fun toDTO(sec: SecurityLevel): SecurityLevelDTO = when (sec) {
        SecurityLevel.SAFE -> SecurityLevelDTO.SAFE
        SecurityLevel.RESTRICTED -> SecurityLevelDTO.RESTRICTED
        SecurityLevel.HIGH_RISK -> SecurityLevelDTO.HIGH_RISK
    }

    fun toDomain(dto: SecurityLevelDTO): SecurityLevel = when (dto) {
        SecurityLevelDTO.SAFE -> SecurityLevel.SAFE
        SecurityLevelDTO.RESTRICTED -> SecurityLevel.RESTRICTED
        SecurityLevelDTO.HIGH_RISK -> SecurityLevel.HIGH_RISK
    }

    fun toDTO(plan: ExecutionPlan): ExecutionPlanDTO = ExecutionPlanDTO(
        planId = plan.planId,
        taskId = plan.taskId,
        steps = plan.steps.map { toDTO(it) },
        estimatedTokenCost = plan.estimatedTokenCost,
        securityLevel = toDTO(plan.securityLevel),
        createdAt = plan.createdAt
    )

    fun toDTO(step: ExecutionStep): ExecutionStepDTO = ExecutionStepDTO(
        stepId = step.stepId,
        actionType = step.actionType,
        target = step.target,
        payload = step.payload.mapValues { it.value.toString() },
        timeoutMs = step.timeoutMs
    )

    fun toDTO(result: ExecutionResult): ExecutionResultDTO = ExecutionResultDTO(
        planId = result.planId,
        taskId = result.taskId,
        isSuccess = result.isSuccess,
        stepResults = result.stepResults.map { toDTO(it) },
        totalDurationMs = result.totalDurationMs,
        executionMetadata = result.executionMetadata
    )

    fun toDTO(stepResult: StepExecutionResult): StepExecutionResultDTO = StepExecutionResultDTO(
        stepId = stepResult.stepId,
        isSuccess = stepResult.isSuccess,
        output = stepResult.output,
        logs = stepResult.logs,
        durationMs = stepResult.durationMs,
        error = stepResult.error
    )

    fun toDTO(verification: VerificationResult): VerificationResultDTO = VerificationResultDTO(
        isVerified = verification.isVerified,
        confidenceScore = verification.confidenceScore,
        checksPassed = verification.checksPassed,
        checksFailed = verification.checksFailed,
        details = verification.details
    )

    fun toDTO(verdict: JudgeVerdict): JudgeVerdictDTO = JudgeVerdictDTO(
        isApproved = verdict.isApproved,
        score = verdict.score,
        summary = verdict.summary,
        feedback = verdict.feedback,
        completedAt = verdict.completedAt
    )

    fun toDTO(state: WorkflowState): WorkflowStateDTO = when (state) {
        WorkflowState.IDLE -> WorkflowStateDTO.IDLE
        WorkflowState.TASK_RECEIVED -> WorkflowStateDTO.TASK_RECEIVED
        WorkflowState.PLANNING -> WorkflowStateDTO.PLANNING
        WorkflowState.GOVERNANCE_CHECK -> WorkflowStateDTO.GOVERNANCE_CHECK
        WorkflowState.READY_FOR_EXECUTION -> WorkflowStateDTO.READY_FOR_EXECUTION
        WorkflowState.EXECUTING -> WorkflowStateDTO.EXECUTING
        WorkflowState.VERIFYING -> WorkflowStateDTO.VERIFYING
        WorkflowState.JUDGING -> WorkflowStateDTO.JUDGING
        WorkflowState.COMPLETED -> WorkflowStateDTO.COMPLETED
        WorkflowState.FAILED -> WorkflowStateDTO.FAILED
        WorkflowState.BLOCKED_BY_GOVERNOR -> WorkflowStateDTO.BLOCKED_BY_GOVERNOR
    }

    fun toDTO(snapshot: WorkflowStateSnapshot): WorkflowSnapshotDTO = WorkflowSnapshotDTO(
        state = toDTO(snapshot.state),
        currentTaskId = snapshot.currentTaskId,
        currentPlanId = snapshot.currentPlanId,
        statusMessage = snapshot.statusMessage,
        timestamp = snapshot.timestamp
    )

    fun toDTO(eval: GovernorEvaluation): GovernorEvaluationDTO = GovernorEvaluationDTO(
        decision = when (eval.decision) {
            GovernorDecision.ALLOWED -> GovernorDecisionDTO.ALLOWED
            GovernorDecision.BLOCKED -> GovernorDecisionDTO.BLOCKED
            GovernorDecision.CONDITIONAL_APPROVAL -> GovernorDecisionDTO.CONDITIONAL_APPROVAL
        },
        reason = eval.reason,
        securityLevel = toDTO(eval.securityLevel),
        requiredModifications = eval.requiredModifications,
        evaluatedAt = eval.timestamp
    )

    fun toDTO(entry: MemoryEntry): MemoryRecordDTO = MemoryRecordDTO(
        id = entry.id,
        key = entry.key,
        value = entry.value,
        tags = entry.tags,
        timestamp = entry.timestamp
    )
}
