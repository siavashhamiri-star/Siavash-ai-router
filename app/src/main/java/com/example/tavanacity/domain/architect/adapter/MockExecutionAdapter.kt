package com.example.tavanacity.domain.architect.adapter

import com.example.tavanacity.domain.architect.model.ExecutionStep
import com.example.tavanacity.domain.architect.model.StepExecutionResult
import kotlinx.coroutines.delay

/**
 * Deterministic Mock Execution Adapter for TAVANA Master Architect Foundation.
 * Does not invoke any real shell, network deployment, or GitHub commands.
 */
class MockExecutionAdapter(
    override val adapterId: String = "mock_execution_adapter",
    private val simulatedLatencyMs: Long = 50L
) : ExecutionAdapter {

    override val supportedActionTypes: Set<String> = setOf(
        "MOCK_CODE_ANALYSIS",
        "MOCK_FILE_READ",
        "MOCK_FILE_WRITE",
        "MOCK_TEST_RUN",
        "MOCK_DIAGNOSTIC",
        "MOCK_SYNTHESIS"
    )

    override suspend fun executeStep(step: ExecutionStep): StepExecutionResult {
        val startTime = System.currentTimeMillis()
        if (simulatedLatencyMs > 0) {
            delay(simulatedLatencyMs)
        }

        if (!supportedActionTypes.contains(step.actionType)) {
            return StepExecutionResult(
                stepId = step.stepId,
                isSuccess = false,
                output = "",
                logs = listOf("Unsupported action type: ${step.actionType}"),
                durationMs = System.currentTimeMillis() - startTime,
                error = "Action ${step.actionType} is not supported by $adapterId"
            )
        }

        val logs = mutableListOf<String>()
        logs.add("Executing ${step.actionType} on target: ${step.target}")

        val output = when (step.actionType) {
            "MOCK_CODE_ANALYSIS" -> {
                logs.add("Analyzed structure for target: ${step.target}")
                "تحلیل کد برای ${step.target} با موفقیت انجام شد. معماری لایه‌ای و اصول Clean Architecture رعایت شده است."
            }
            "MOCK_FILE_READ" -> {
                logs.add("Read mock file: ${step.target}")
                "محتوای شبیه‌سازی‌شده فایل ${step.target} با ساختار معتبر خوانده شد."
            }
            "MOCK_FILE_WRITE" -> {
                logs.add("Simulated file write to: ${step.target}")
                "تغییرات ساختاری در ${step.target} در حافظه موقت ثبت گردید."
            }
            "MOCK_TEST_RUN" -> {
                logs.add("Simulated test suite execution for: ${step.target}")
                "تمامی تست‌های آزمایشی (${step.target}) با موفقیت پاس شدند (0 failures)."
            }
            "MOCK_DIAGNOSTIC" -> {
                logs.add("Diagnostics complete for: ${step.target}")
                "عیب‌یابی سیستمی بدون خطا و با وضعیت نرمال پایان یافت."
            }
            "MOCK_SYNTHESIS" -> {
                logs.add("Synthesizing solution components")
                "سنتز راه‌حل معماری برای ${step.target} با موفقیت انجام شد."
            }
            else -> {
                logs.add("Default mock execution completed")
                "عملیات شبیه‌سازی با موفقیت انجام شد."
            }
        }

        val duration = System.currentTimeMillis() - startTime
        logs.add("Step completed in ${duration}ms")

        return StepExecutionResult(
            stepId = step.stepId,
            isSuccess = true,
            output = output,
            logs = logs,
            durationMs = duration
        )
    }
}
