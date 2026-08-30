package com.example.tavanacity.domain.architect.adapter

import com.example.tavanacity.domain.architect.model.ExecutionStep
import com.example.tavanacity.domain.architect.model.StepExecutionResult

/**
 * Interface for adapters that execute concrete operations (e.g. mock tools, code processors).
 */
interface ExecutionAdapter {
    val adapterId: String
    val supportedActionTypes: Set<String>

    suspend fun executeStep(step: ExecutionStep): StepExecutionResult
}
