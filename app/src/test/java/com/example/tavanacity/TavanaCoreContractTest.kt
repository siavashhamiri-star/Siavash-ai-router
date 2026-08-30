package com.example.tavanacity

import com.example.tavanacity.core.contract.execution.SecurityLevelDTO
import com.example.tavanacity.core.contract.mapper.ContractMappers
import com.example.tavanacity.core.contract.task.ArchitectTaskTypeDTO
import com.example.tavanacity.core.contract.task.TaskPriorityDTO
import com.example.tavanacity.core.contract.task.TaskRequestDTO
import com.example.tavanacity.core.contract.task.TaskStatusDTO
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TavanaCoreContractTest {

    @Test
    fun testTaskRequestToDomainMapping() {
        val request = TaskRequestDTO(
            title = "طراحی قرارداد مرکزی پلتفرم",
            description = "جداسازی کامل مغز مرکزی از کلاینت‌های اندروید و وب",
            taskType = ArchitectTaskTypeDTO.ARCHITECTURE_DESIGN,
            priority = TaskPriorityDTO.CRITICAL,
            parameters = mapOf("scope" to "contract_only"),
            clientId = "pwa-client-01",
            clientPlatform = "PWA_WEB"
        )

        val domainTask = ContractMappers.toDomain(request)
        assertEquals("طراحی قرارداد مرکزی پلتفرم", domainTask.title)
        assertEquals(ArchitectTaskType.ARCHITECTURE_DESIGN, domainTask.taskType)
        assertEquals(TaskPriority.CRITICAL, domainTask.priority)
        assertEquals("contract_only", domainTask.parameters["scope"])

        val taskDTO = ContractMappers.toDTO(domainTask, TaskStatusDTO.PLANNING)
        assertEquals(domainTask.id, taskDTO.id)
        assertEquals(TaskStatusDTO.PLANNING, taskDTO.status)
        assertEquals(ArchitectTaskTypeDTO.ARCHITECTURE_DESIGN, taskDTO.taskType)
    }

    @Test
    fun testExecutionPlanAndResultMapping() {
        val domainPlan = ExecutionPlan(
            taskId = "task-123",
            steps = listOf(
                ExecutionStep(
                    actionType = "MOCK_CODE_ANALYSIS",
                    target = "CoreContract",
                    payload = mapOf("version" to "2.0")
                )
            ),
            estimatedTokenCost = 4,
            securityLevel = SecurityLevel.RESTRICTED
        )

        val planDTO = ContractMappers.toDTO(domainPlan)
        assertEquals(domainPlan.planId, planDTO.planId)
        assertEquals(1, planDTO.steps.size)
        assertEquals(SecurityLevelDTO.RESTRICTED, planDTO.securityLevel)
        assertEquals("CoreContract", planDTO.steps[0].target)

        val domainResult = ExecutionResult(
            planId = domainPlan.planId,
            taskId = "task-123",
            isSuccess = true,
            stepResults = listOf(
                StepExecutionResult(
                    stepId = "step-01",
                    isSuccess = true,
                    output = "تحلیل با موفقیت انجام شد.",
                    durationMs = 150L
                )
            ),
            totalDurationMs = 150L,
            executionMetadata = mapOf("engine" to "mock")
        )

        val resultDTO = ContractMappers.toDTO(domainResult)
        assertTrue(resultDTO.isSuccess)
        assertEquals(1, resultDTO.stepResults.size)
        assertEquals("تحلیل با موفقیت انجام شد.", resultDTO.stepResults[0].output)
    }

    @Test
    fun testVerificationAndJudgeMapping() {
        val domainVerification = VerificationResult(
            isVerified = true,
            confidenceScore = 0.95,
            checksPassed = listOf("ساختار بدون وابستگی است", "تمامی DTOها معتبرند"),
            checksFailed = emptyList(),
            details = "صحت‌سنجی قرارداد مرکزی با موفقیت تایید شد."
        )
        val verificationDTO = ContractMappers.toDTO(domainVerification)
        assertTrue(verificationDTO.isVerified)
        assertEquals(0.95, verificationDTO.confidenceScore, 0.001)

        val domainVerdict = JudgeVerdict(
            isApproved = true,
            score = 92,
            summary = "معماری قرارداد مرکزی پلتفرم تایید شد.",
            feedback = listOf("جداسازی بدون نقص لایه‌ها"),
            completedAt = 1700000000L
        )
        val verdictDTO = ContractMappers.toDTO(domainVerdict)
        assertTrue(verdictDTO.isApproved)
        assertEquals(92, verdictDTO.score)
        assertEquals(1700000000L, verdictDTO.completedAt)
    }

    @Test
    fun testWorkflowAndGovernorMapping() {
        val snapshot = WorkflowStateSnapshot(
            state = WorkflowState.GOVERNANCE_CHECK,
            currentTaskId = "t-100",
            currentPlanId = "p-200",
            statusMessage = "بررسی ناظر"
        )
        val snapshotDTO = ContractMappers.toDTO(snapshot)
        assertEquals(com.example.tavanacity.core.contract.workflow.WorkflowStateDTO.GOVERNANCE_CHECK, snapshotDTO.state)
        assertEquals("t-100", snapshotDTO.currentTaskId)

        val govEval = GovernorEvaluation(
            decision = GovernorDecision.ALLOWED,
            reason = "طرح امن است.",
            securityLevel = SecurityLevel.SAFE
        )
        val govDTO = ContractMappers.toDTO(govEval)
        assertEquals(com.example.tavanacity.core.contract.governor.GovernorDecisionDTO.ALLOWED, govDTO.decision)
        assertEquals(SecurityLevelDTO.SAFE, govDTO.securityLevel)
    }

    @Test
    fun testMemoryMapping() {
        val entry = MemoryEntry(
            id = "mem-1",
            key = "arch_standard",
            value = "Clean Architecture v2",
            tags = setOf("ARCH", "SPEC")
        )
        val memoryDTO = ContractMappers.toDTO(entry)
        assertEquals("mem-1", memoryDTO.id)
        assertEquals("arch_standard", memoryDTO.key)
        assertTrue(memoryDTO.tags.contains("ARCH"))
    }
}
