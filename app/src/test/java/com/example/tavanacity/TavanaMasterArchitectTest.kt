package com.example.tavanacity

import com.example.tavanacity.domain.architect.adapter.ExecutionAdapter
import com.example.tavanacity.domain.architect.adapter.MockExecutionAdapter
import com.example.tavanacity.domain.architect.gateway.ExecutionGateway
import com.example.tavanacity.domain.architect.governor.DefaultGovernor
import com.example.tavanacity.domain.architect.governor.Governor
import com.example.tavanacity.domain.architect.governor.GovernorDecision
import com.example.tavanacity.domain.architect.judge.DefaultArchitectJudge
import com.example.tavanacity.domain.architect.memory.InMemoryArchitectMemory
import com.example.tavanacity.domain.architect.model.ArchitectTask
import com.example.tavanacity.domain.architect.model.ArchitectTaskType
import com.example.tavanacity.domain.architect.model.ExecutionPlan
import com.example.tavanacity.domain.architect.model.ExecutionStep
import com.example.tavanacity.domain.architect.model.SecurityLevel
import com.example.tavanacity.domain.architect.model.StepExecutionResult
import com.example.tavanacity.domain.architect.model.TaskPriority
import com.example.tavanacity.domain.architect.orchestrator.TavanaMasterOrchestrator
import com.example.tavanacity.domain.architect.state.WorkflowState
import com.example.tavanacity.domain.architect.state.WorkflowStateMachine
import com.example.tavanacity.domain.architect.verification.DefaultVerificationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TavanaMasterArchitectTest {

    private lateinit var stateMachine: WorkflowStateMachine
    private lateinit var memory: InMemoryArchitectMemory
    private lateinit var governor: Governor
    private lateinit var mockAdapter: MockExecutionAdapter
    private lateinit var gateway: ExecutionGateway
    private lateinit var verificationEngine: DefaultVerificationEngine
    private lateinit var judge: DefaultArchitectJudge
    private lateinit var orchestrator: TavanaMasterOrchestrator

    private val testDispatcher = Dispatchers.Unconfined

    @Before
    fun setup() {
        stateMachine = WorkflowStateMachine()
        memory = InMemoryArchitectMemory()
        governor = DefaultGovernor()
        mockAdapter = MockExecutionAdapter(simulatedLatencyMs = 0L)
        gateway = ExecutionGateway(listOf(mockAdapter))
        verificationEngine = DefaultVerificationEngine()
        judge = DefaultArchitectJudge()

        orchestrator = TavanaMasterOrchestrator(
            governor = governor,
            executionGateway = gateway,
            verificationEngine = verificationEngine,
            judge = judge,
            memory = memory,
            stateMachine = stateMachine,
            ioDispatcher = testDispatcher
        )
    }

    // 1. Full Lifecycle Success Test
    @Test
    fun testFullArchitectPipelineSuccess() = runTest {
        val task = ArchitectTask(
            title = "طراحی معماری هسته معمار ارشد",
            description = "تحلیل ساختار لایه‌ای و استخراج بلوک‌های اصلی سیستم",
            taskType = ArchitectTaskType.ARCHITECTURE_DESIGN,
            priority = TaskPriority.HIGH
        )

        val result = orchestrator.executeTask(task)

        // Verify result
        assertTrue("Pipeline execution must be successful", result.isSuccessful)
        assertNotNull("Plan must be generated", result.plan)
        assertNotNull("Execution result must be present", result.executionResult)
        assertTrue("Execution must be successful", result.executionResult?.isSuccess == true)
        assertNotNull("Verification must be present", result.verificationResult)
        assertTrue("Verification must pass", result.verificationResult?.isVerified == true)
        assertNotNull("Judge verdict must be present", result.verdict)
        assertTrue("Judge must approve", result.verdict?.isApproved == true)
        assertTrue("Judge score must be >= 70", (result.verdict?.score ?: 0) >= 70)

        // Verify final state
        assertEquals(WorkflowState.COMPLETED, stateMachine.currentState.value.state)

        // Verify memory persistence
        val storedTask = memory.retrieve("task_${task.id}")
        assertNotNull("Task must be stored in memory", storedTask)
        assertEquals(task.description, storedTask?.value)

        val storedExec = memory.retrieve("exec_${result.plan?.planId}")
        assertNotNull("Execution summary must be stored in memory", storedExec)
    }

    // 2. State Machine History Tracking
    @Test
    fun testWorkflowStateMachineHistoryTransitions() = runTest {
        val task = ArchitectTask(
            title = "تست وضعیت‌ها",
            description = "بررسی ترنزیشن‌های ترتیبی ماشین وضعیت",
            taskType = ArchitectTaskType.ANALYSIS
        )

        orchestrator.executeTask(task)

        val history = stateMachine.getHistory()
        val statesInHistory = history.map { it.state }

        assertTrue(statesInHistory.contains(WorkflowState.TASK_RECEIVED))
        assertTrue(statesInHistory.contains(WorkflowState.PLANNING))
        assertTrue(statesInHistory.contains(WorkflowState.GOVERNANCE_CHECK))
        assertTrue(statesInHistory.contains(WorkflowState.READY_FOR_EXECUTION))
        assertTrue(statesInHistory.contains(WorkflowState.EXECUTING))
        assertTrue(statesInHistory.contains(WorkflowState.VERIFYING))
        assertTrue(statesInHistory.contains(WorkflowState.JUDGING))
        assertTrue(statesInHistory.contains(WorkflowState.COMPLETED))
    }

    // 3. Governor Blocks Forbidden Tasks
    @Test
    fun testGovernorBlocksForbiddenTask() = runTest {
        val maliciousTask = ArchitectTask(
            title = "عملیات خطرناک DROP_TABLE",
            description = "تلاش برای اجرای دستور DROP_TABLE در پایگاه داده",
            taskType = ArchitectTaskType.CODE_MODIFICATION
        )

        val result = orchestrator.executeTask(maliciousTask)

        assertFalse("Malicious task must not succeed", result.isSuccessful)
        assertEquals(WorkflowState.BLOCKED_BY_GOVERNOR, stateMachine.currentState.value.state)
        assertTrue(result.errorMessage?.contains("مسدودشده") == true)
        assertNotNull("Memory should record task receipt even if blocked", memory.retrieve("task_${maliciousTask.id}"))
    }

    // 4. Governor Blocks Illegal Action Type in Plan
    @Test
    fun testGovernorBlocksIllegalActionPlan() = runTest {
        val task = ArchitectTask(
            title = "تست پلن غیرمجاز",
            description = "پلنی با اکشن ناشناخته",
            taskType = ArchitectTaskType.CUSTOM
        )

        val customGovernor = DefaultGovernor()
        val illegalPlan = ExecutionPlan(
            taskId = task.id,
            steps = listOf(
                ExecutionStep(actionType = "UNAUTHORIZED_ROOT_SHELL", target = "/root")
            ),
            securityLevel = SecurityLevel.HIGH_RISK
        )

        val evaluation = customGovernor.evaluatePlan(task, illegalPlan)
        assertEquals(GovernorDecision.BLOCKED, evaluation.decision)
        assertTrue(evaluation.reason.contains("مجاز نیست"))
    }

    // 5. Verification Engine Rejects Failed Steps
    @Test
    fun testVerificationEngineRejectsFailedExecution() = runTest {
        val task = ArchitectTask(title = "تست شکست", description = "شبیه‌سازی خطا")
        val plan = ExecutionPlan(
            taskId = task.id,
            steps = listOf(ExecutionStep(actionType = "MOCK_CODE_ANALYSIS", target = "TestTarget"))
        )

        val failingExecResult = com.example.tavanacity.domain.architect.model.ExecutionResult(
            planId = plan.planId,
            taskId = task.id,
            isSuccess = false,
            stepResults = listOf(
                StepExecutionResult(
                    stepId = "step_1",
                    isSuccess = false,
                    output = "",
                    error = "Synthetic error"
                )
            ),
            totalDurationMs = 10L
        )

        val verification = verificationEngine.verifyExecution(task, plan, failingExecResult)
        assertFalse("Verification should fail when execution failed", verification.isVerified)
        assertTrue(verification.checksFailed.isNotEmpty())

        val verdict = judge.evaluate(task, plan, failingExecResult, verification)
        assertFalse("Judge must not approve unverified execution", verdict.isApproved)
    }

    // 6. Memory Store and Tag Querying
    @Test
    fun testMemoryStoreAndRetrieve() = runTest {
        memory.store("config_arch", "CleanArchitecture-v1", setOf("CONFIG", "ARCH"))
        memory.store("config_db", "RoomDB-v2", setOf("CONFIG", "DATABASE"))

        val archEntry = memory.retrieve("config_arch")
        assertNotNull(archEntry)
        assertEquals("CleanArchitecture-v1", archEntry?.value)

        val configEntries = memory.findByTag("CONFIG")
        assertEquals(2, configEntries.size)

        val dbEntries = memory.findByTag("DATABASE")
        assertEquals(1, dbEntries.size)
        assertEquals("RoomDB-v2", dbEntries.first().value)
    }
}
