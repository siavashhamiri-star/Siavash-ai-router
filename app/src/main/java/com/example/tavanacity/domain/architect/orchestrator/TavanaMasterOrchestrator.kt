package com.example.tavanacity.domain.architect.orchestrator

import com.example.tavanacity.domain.architect.gateway.ExecutionGateway
import com.example.tavanacity.domain.architect.governor.DefaultGovernor
import com.example.tavanacity.domain.architect.governor.Governor
import com.example.tavanacity.domain.architect.governor.GovernorDecision
import com.example.tavanacity.domain.architect.judge.DefaultArchitectJudge
import com.example.tavanacity.domain.architect.judge.JudgeInterface
import com.example.tavanacity.domain.architect.memory.InMemoryArchitectMemory
import com.example.tavanacity.domain.architect.memory.MemoryInterface
import com.example.tavanacity.domain.architect.model.ArchitectPipelineResult
import com.example.tavanacity.domain.architect.model.ArchitectTask
import com.example.tavanacity.domain.architect.model.ArchitectTaskType
import com.example.tavanacity.domain.architect.model.ExecutionPlan
import com.example.tavanacity.domain.architect.model.ExecutionStep
import com.example.tavanacity.domain.architect.model.SecurityLevel
import com.example.tavanacity.domain.architect.state.WorkflowState
import com.example.tavanacity.domain.architect.state.WorkflowStateMachine
import com.example.tavanacity.domain.architect.state.WorkflowStateSnapshot
import com.example.tavanacity.domain.architect.verification.DefaultVerificationEngine
import com.example.tavanacity.domain.architect.verification.VerificationInterface
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * TAVANA Master Orchestrator (Foundation Layer)
 * Coordinates the full lifecycle:
 * TASK -> ORCHESTRATOR -> GOVERNOR -> EXECUTION GATEWAY -> MOCK EXECUTION -> VERIFICATION -> JUDGE -> RESULT
 */
class TavanaMasterOrchestrator(
    private val governor: Governor = DefaultGovernor(),
    private val executionGateway: ExecutionGateway = ExecutionGateway(),
    private val verificationEngine: VerificationInterface = DefaultVerificationEngine(),
    private val judge: JudgeInterface = DefaultArchitectJudge(),
    private val memory: MemoryInterface = InMemoryArchitectMemory(),
    private val stateMachine: WorkflowStateMachine = WorkflowStateMachine(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    val workflowState: StateFlow<WorkflowStateSnapshot> = stateMachine.currentState

    /**
     * Executes the full pipeline for a given task.
     */
    suspend fun executeTask(task: ArchitectTask): ArchitectPipelineResult = withContext(ioDispatcher) {
        val startTime = System.currentTimeMillis()

        // 1. Task Received
        stateMachine.transitionTo(
            newState = WorkflowState.TASK_RECEIVED,
            taskId = task.id,
            statusMessage = "وظیفه دریافت شد: ${task.title}"
        )
        memory.store("task_${task.id}", task.description, setOf("TASK", task.taskType.name))

        // 2. Governor Pre-Task Evaluation
        val taskGovEvaluation = governor.evaluateTask(task)
        if (taskGovEvaluation.decision == GovernorDecision.BLOCKED) {
            stateMachine.transitionTo(
                newState = WorkflowState.BLOCKED_BY_GOVERNOR,
                taskId = task.id,
                statusMessage = "مسدودشده توسط ناظر: ${taskGovEvaluation.reason}"
            )
            return@withContext ArchitectPipelineResult(
                taskId = task.id,
                isSuccessful = false,
                task = task,
                plan = null,
                executionResult = null,
                verificationResult = null,
                verdict = null,
                finalOutput = "عملیات توسط ناظر امنیتی مسدود شد: ${taskGovEvaluation.reason}",
                totalTimeMs = System.currentTimeMillis() - startTime,
                errorMessage = taskGovEvaluation.reason
            )
        }

        // 3. Planning Phase
        stateMachine.transitionTo(
            newState = WorkflowState.PLANNING,
            taskId = task.id,
            statusMessage = "در حال تدوین پلن اجرایی..."
        )
        val plan = generateExecutionPlan(task)
        memory.store("plan_${plan.planId}", plan.steps.joinToString { it.actionType }, setOf("PLAN", task.id))

        // 4. Governor Plan Evaluation
        stateMachine.transitionTo(
            newState = WorkflowState.GOVERNANCE_CHECK,
            taskId = task.id,
            planId = plan.planId,
            statusMessage = "در حال بررسی تطابق و امنیت پلن در ناظر..."
        )
        val planGovEvaluation = governor.evaluatePlan(task, plan)
        if (planGovEvaluation.decision == GovernorDecision.BLOCKED) {
            stateMachine.transitionTo(
                newState = WorkflowState.BLOCKED_BY_GOVERNOR,
                taskId = task.id,
                planId = plan.planId,
                statusMessage = "پلن توسط ناظر رد شد: ${planGovEvaluation.reason}"
            )
            return@withContext ArchitectPipelineResult(
                taskId = task.id,
                isSuccessful = false,
                task = task,
                plan = plan,
                executionResult = null,
                verificationResult = null,
                verdict = null,
                finalOutput = "پلن اجرایی توسط ناظر مسدود شد: ${planGovEvaluation.reason}",
                totalTimeMs = System.currentTimeMillis() - startTime,
                errorMessage = planGovEvaluation.reason
            )
        }

        // 5. Ready & Execution via Execution Gateway (Mock execution in foundation)
        stateMachine.transitionTo(
            newState = WorkflowState.READY_FOR_EXECUTION,
            taskId = task.id,
            planId = plan.planId,
            statusMessage = "آماده ارسال به درگاه اجرا..."
        )

        stateMachine.transitionTo(
            newState = WorkflowState.EXECUTING,
            taskId = task.id,
            planId = plan.planId,
            statusMessage = "در حال اجرای گام‌های پلن در درگاه..."
        )
        val executionResult = executionGateway.executePlan(plan)
        memory.store(
            "exec_${executionResult.planId}",
            "Success: ${executionResult.isSuccess}, Duration: ${executionResult.totalDurationMs}ms",
            setOf("EXECUTION", task.id)
        )

        // 6. Verification Phase
        stateMachine.transitionTo(
            newState = WorkflowState.VERIFYING,
            taskId = task.id,
            planId = plan.planId,
            statusMessage = "در حال صحت‌سنجی خروجی‌های اجرا..."
        )
        val verificationResult = verificationEngine.verifyExecution(task, plan, executionResult)

        // 7. Judge Phase
        stateMachine.transitionTo(
            newState = WorkflowState.JUDGING,
            taskId = task.id,
            planId = plan.planId,
            statusMessage = "در حال ارزیابی کیفی و داوری نهایی..."
        )
        val verdict = judge.evaluate(task, plan, executionResult, verificationResult)

        // 8. Final Result Assembly
        val isOverallSuccess = executionResult.isSuccess && verificationResult.isVerified && verdict.isApproved
        val finalState = if (isOverallSuccess) WorkflowState.COMPLETED else WorkflowState.FAILED
        val statusMsg = if (isOverallSuccess) "ماموریت با موفقیت کامل انجام شد." else "ماموریت به نتیجه مطلوب نرسید."

        stateMachine.transitionTo(
            newState = finalState,
            taskId = task.id,
            planId = plan.planId,
            statusMessage = statusMsg
        )

        val aggregatedOutput = buildString {
            appendLine("=== گزارش خروجی معمار ارشد توانا (TAVANA Master Architect) ===")
            appendLine("عنوان ماموریت: ${task.title}")
            appendLine("وضعیت نهایی: ${if (isOverallSuccess) "موفقیت‌آمیز" else "ناموفق"}")
            appendLine("امتیاز داوری: ${verdict.score} / ۱۰۰")
            appendLine("خلاصه داوری: ${verdict.summary}")
            appendLine("\n[خروجی گام‌های اجرا شده]:")
            executionResult.stepResults.forEachIndexed { index, stepRes ->
                appendLine("${index + 1}. [${stepRes.stepId.take(8)}] ${stepRes.output}")
            }
            appendLine("\n[صحت‌سنجی]: ${verificationResult.details}")
        }

        ArchitectPipelineResult(
            taskId = task.id,
            isSuccessful = isOverallSuccess,
            task = task,
            plan = plan,
            executionResult = executionResult,
            verificationResult = verificationResult,
            verdict = verdict,
            finalOutput = aggregatedOutput,
            totalTimeMs = System.currentTimeMillis() - startTime,
            errorMessage = if (!isOverallSuccess) verdict.summary else null
        )
    }

    /**
     * Internal planner mapping tasks to deterministic Mock Execution steps.
     */
    private fun generateExecutionPlan(task: ArchitectTask): ExecutionPlan {
        val steps = mutableListOf<ExecutionStep>()
        when (task.taskType) {
            ArchitectTaskType.ANALYSIS -> {
                steps.add(ExecutionStep(actionType = "MOCK_CODE_ANALYSIS", target = task.title))
                steps.add(ExecutionStep(actionType = "MOCK_SYNTHESIS", target = "Architecture Analysis Report"))
            }
            ArchitectTaskType.CODE_MODIFICATION -> {
                steps.add(ExecutionStep(actionType = "MOCK_FILE_READ", target = "src/main/AppModule"))
                steps.add(ExecutionStep(actionType = "MOCK_FILE_WRITE", target = "src/main/AppModule"))
                steps.add(ExecutionStep(actionType = "MOCK_TEST_RUN", target = "UnitTests"))
            }
            ArchitectTaskType.ARCHITECTURE_DESIGN -> {
                steps.add(ExecutionStep(actionType = "MOCK_CODE_ANALYSIS", target = "CoreDomain"))
                steps.add(ExecutionStep(actionType = "MOCK_SYNTHESIS", target = "Target Architecture Blueprint"))
            }
            ArchitectTaskType.VERIFICATION -> {
                steps.add(ExecutionStep(actionType = "MOCK_TEST_RUN", target = "AllTestSuites"))
                steps.add(ExecutionStep(actionType = "MOCK_DIAGNOSTIC", target = "SystemIntegrity"))
            }
            ArchitectTaskType.DIAGNOSTIC -> {
                steps.add(ExecutionStep(actionType = "MOCK_DIAGNOSTIC", target = task.title))
            }
            ArchitectTaskType.CUSTOM -> {
                steps.add(ExecutionStep(actionType = "MOCK_CODE_ANALYSIS", target = task.title))
                steps.add(ExecutionStep(actionType = "MOCK_SYNTHESIS", target = "Custom Execution"))
            }
        }

        return ExecutionPlan(
            planId = UUID.randomUUID().toString(),
            taskId = task.id,
            steps = steps,
            estimatedTokenCost = steps.size * 2,
            securityLevel = SecurityLevel.SAFE
        )
    }
}
