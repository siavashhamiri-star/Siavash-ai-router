package com.example.tavanacity.domain.architect.gateway

import com.example.tavanacity.domain.architect.adapter.ExecutionAdapter
import com.example.tavanacity.domain.architect.adapter.MockExecutionAdapter
import com.example.tavanacity.domain.architect.model.ExecutionPlan
import com.example.tavanacity.domain.architect.model.ExecutionResult
import com.example.tavanacity.domain.architect.model.StepExecutionResult
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Execution Gateway that coordinates action execution across registered adapters
 * while maintaining timeout safety, error containment, and execution logging.
 */
class ExecutionGateway(
    initialAdapters: List<ExecutionAdapter> = listOf(MockExecutionAdapter())
) {

    private val registeredAdapters = mutableListOf<ExecutionAdapter>().apply {
        addAll(initialAdapters)
    }

    fun registerAdapter(adapter: ExecutionAdapter) {
        synchronized(registeredAdapters) {
            registeredAdapters.add(adapter)
        }
    }

    suspend fun executePlan(plan: ExecutionPlan): ExecutionResult {
        val startTime = System.currentTimeMillis()
        val stepResults = mutableListOf<StepExecutionResult>()
        var overallSuccess = true

        for (step in plan.steps) {
            val adapter = findAdapterForAction(step.actionType)
            if (adapter == null) {
                val failureResult = StepExecutionResult(
                    stepId = step.stepId,
                    isSuccess = false,
                    output = "",
                    logs = listOf("No adapter found for action type: ${step.actionType}"),
                    durationMs = 0L,
                    error = "Adapter unavailable for action: ${step.actionType}"
                )
                stepResults.add(failureResult)
                overallSuccess = false
                break
            }

            val stepStart = System.currentTimeMillis()
            val result = withTimeoutOrNull(step.timeoutMs) {
                try {
                    adapter.executeStep(step)
                } catch (e: Exception) {
                    StepExecutionResult(
                        stepId = step.stepId,
                        isSuccess = false,
                        output = "",
                        logs = listOf("Exception during step execution: ${e.message}"),
                        durationMs = System.currentTimeMillis() - stepStart,
                        error = e.message ?: "Unknown execution error"
                    )
                }
            } ?: StepExecutionResult(
                stepId = step.stepId,
                isSuccess = false,
                output = "",
                logs = listOf("Step timed out after ${step.timeoutMs}ms"),
                durationMs = step.timeoutMs,
                error = "Execution timed out"
            )

            stepResults.add(result)
            if (!result.isSuccess) {
                overallSuccess = false
                break
            }
        }

        val totalDuration = System.currentTimeMillis() - startTime
        return ExecutionResult(
            planId = plan.planId,
            taskId = plan.taskId,
            isSuccess = overallSuccess,
            stepResults = stepResults,
            totalDurationMs = totalDuration,
            executionMetadata = mapOf(
                "stepsCount" to plan.steps.size.toString(),
                "executedCount" to stepResults.size.toString(),
                "gatewayVersion" to "1.0.0-foundation"
            )
        )
    }

    private fun findAdapterForAction(actionType: String): ExecutionAdapter? {
        synchronized(registeredAdapters) {
            return registeredAdapters.firstOrNull { it.supportedActionTypes.contains(actionType) }
        }
    }
}
