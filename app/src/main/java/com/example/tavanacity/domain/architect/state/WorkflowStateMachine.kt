package com.example.tavanacity.domain.architect.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * States of the TAVANA Master Architect execution lifecycle.
 */
enum class WorkflowState {
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
 * Structured state event with timestamp and descriptive context.
 */
data class WorkflowStateSnapshot(
    val state: WorkflowState = WorkflowState.IDLE,
    val currentTaskId: String? = null,
    val currentPlanId: String? = null,
    val statusMessage: String = "آماده به کار",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * State machine managing lifecycle transitions with validation.
 */
class WorkflowStateMachine {

    private val _currentState = MutableStateFlow(WorkflowStateSnapshot())
    val currentState: StateFlow<WorkflowStateSnapshot> = _currentState.asStateFlow()

    private val stateHistory = mutableListOf<WorkflowStateSnapshot>()

    fun getHistory(): List<WorkflowStateSnapshot> = synchronized(stateHistory) {
        stateHistory.toList()
    }

    /**
     * Attempts a validated state transition.
     */
    @Synchronized
    fun transitionTo(
        newState: WorkflowState,
        taskId: String? = _currentState.value.currentTaskId,
        planId: String? = _currentState.value.currentPlanId,
        statusMessage: String
    ): Boolean {
        val current = _currentState.value.state
        if (isValidTransition(current, newState)) {
            val snapshot = WorkflowStateSnapshot(
                state = newState,
                currentTaskId = taskId,
                currentPlanId = planId,
                statusMessage = statusMessage,
                timestamp = System.currentTimeMillis()
            )
            _currentState.value = snapshot
            stateHistory.add(snapshot)
            return true
        }
        return false
    }

    /**
     * Resets the state machine back to IDLE.
     */
    @Synchronized
    fun reset() {
        val snapshot = WorkflowStateSnapshot(
            state = WorkflowState.IDLE,
            currentTaskId = null,
            currentPlanId = null,
            statusMessage = "آماده به کار",
            timestamp = System.currentTimeMillis()
        )
        _currentState.value = snapshot
        stateHistory.add(snapshot)
    }

    private fun isValidTransition(from: WorkflowState, to: WorkflowState): Boolean {
        if (to == WorkflowState.IDLE || to == WorkflowState.FAILED) return true
        return when (from) {
            WorkflowState.IDLE -> to == WorkflowState.TASK_RECEIVED
            WorkflowState.TASK_RECEIVED -> to == WorkflowState.PLANNING || to == WorkflowState.BLOCKED_BY_GOVERNOR || to == WorkflowState.FAILED
            WorkflowState.PLANNING -> to == WorkflowState.GOVERNANCE_CHECK || to == WorkflowState.FAILED
            WorkflowState.GOVERNANCE_CHECK -> to == WorkflowState.READY_FOR_EXECUTION || to == WorkflowState.BLOCKED_BY_GOVERNOR || to == WorkflowState.FAILED
            WorkflowState.READY_FOR_EXECUTION -> to == WorkflowState.EXECUTING || to == WorkflowState.FAILED
            WorkflowState.EXECUTING -> to == WorkflowState.VERIFYING || to == WorkflowState.FAILED
            WorkflowState.VERIFYING -> to == WorkflowState.JUDGING || to == WorkflowState.FAILED
            WorkflowState.JUDGING -> to == WorkflowState.COMPLETED || to == WorkflowState.FAILED
            WorkflowState.COMPLETED, WorkflowState.BLOCKED_BY_GOVERNOR, WorkflowState.FAILED -> to == WorkflowState.IDLE || to == WorkflowState.TASK_RECEIVED
        }
    }
}
