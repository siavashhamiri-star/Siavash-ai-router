package com.example.tavanacity.core.contract.service

import com.example.tavanacity.core.contract.execution.ExecutionPlanDTO
import com.example.tavanacity.core.contract.execution.ExecutionResultDTO
import com.example.tavanacity.core.contract.governor.ApprovalRequestDTO
import com.example.tavanacity.core.contract.governor.GovernorEvaluationDTO
import com.example.tavanacity.core.contract.judge.JudgeVerdictDTO
import com.example.tavanacity.core.contract.memory.MemoryQueryDTO
import com.example.tavanacity.core.contract.memory.MemoryQueryResultDTO
import com.example.tavanacity.core.contract.memory.MemoryRecordDTO
import com.example.tavanacity.core.contract.provider.ProviderRequestDTO
import com.example.tavanacity.core.contract.provider.ProviderResponseDTO
import com.example.tavanacity.core.contract.task.TaskDTO
import com.example.tavanacity.core.contract.task.TaskRequestDTO
import com.example.tavanacity.core.contract.verification.VerificationResultDTO
import com.example.tavanacity.core.contract.workflow.WorkflowSnapshotDTO
import kotlinx.coroutines.flow.Flow

/**
 * Unified Core Contract Interface for all TAVANA Clients (Android, PWA/Web, Desktop/CLI).
 *
 * This contract defines the exact operations exposed by TAVANA Master Architect.
 * Clients consume this interface without duplicating orchestrator, governance,
 * judge, or verification business logic.
 */
interface TavanaCoreClientContract {

    // --- Task Lifecycle Operations ---
    suspend fun submitTask(request: TaskRequestDTO): TaskDTO
    suspend fun getTask(taskId: String): TaskDTO?
    suspend fun cancelTask(taskId: String): Boolean
    fun observeTaskWorkflow(taskId: String): Flow<WorkflowSnapshotDTO>

    // --- Plan & Governance Operations ---
    suspend fun getExecutionPlan(taskId: String): ExecutionPlanDTO?
    suspend fun evaluatePolicy(taskId: String): GovernorEvaluationDTO
    suspend fun submitApproval(approval: ApprovalRequestDTO): GovernorEvaluationDTO

    // --- Execution, Verification & Judgment Inspection ---
    suspend fun getExecutionResult(taskId: String): ExecutionResultDTO?
    suspend fun getVerificationResult(taskId: String): VerificationResultDTO?
    suspend fun getJudgeVerdict(taskId: String): JudgeVerdictDTO?

    // --- Memory Operations ---
    suspend fun storeMemory(record: MemoryRecordDTO): MemoryRecordDTO
    suspend fun queryMemory(query: MemoryQueryDTO): MemoryQueryResultDTO

    // --- AI Router Provider Operations ---
    suspend fun routeAIRequest(request: ProviderRequestDTO): ProviderResponseDTO
}
