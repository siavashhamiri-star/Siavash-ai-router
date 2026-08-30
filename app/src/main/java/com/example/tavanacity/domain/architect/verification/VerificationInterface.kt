package com.example.tavanacity.domain.architect.verification

import com.example.tavanacity.domain.architect.model.ArchitectTask
import com.example.tavanacity.domain.architect.model.ExecutionPlan
import com.example.tavanacity.domain.architect.model.ExecutionResult
import com.example.tavanacity.domain.architect.model.VerificationResult

/**
 * Interface for verifying execution outputs against task constraints, schema validity,
 * and safety invariants.
 */
interface VerificationInterface {
    suspend fun verifyExecution(
        task: ArchitectTask,
        plan: ExecutionPlan,
        executionResult: ExecutionResult
    ): VerificationResult
}

/**
 * Default verification engine that checks execution completeness, output non-emptiness,
 * error flags, and consistency.
 */
class DefaultVerificationEngine : VerificationInterface {

    override suspend fun verifyExecution(
        task: ArchitectTask,
        plan: ExecutionPlan,
        executionResult: ExecutionResult
    ): VerificationResult {
        val passedChecks = mutableListOf<String>()
        val failedChecks = mutableListOf<String>()

        // Check 1: Execution success flag
        if (executionResult.isSuccess) {
            passedChecks.add("وضعیت کلی اجرای پلن موفق است.")
        } else {
            failedChecks.add("اجرای پلن با خطا مواجه شده است.")
        }

        // Check 2: Step count alignment
        if (executionResult.stepResults.size == plan.steps.size) {
            passedChecks.add("تمامی گام‌های تعریف‌شده (${plan.steps.size}) پردازش شدند.")
        } else {
            failedChecks.add("تعداد گام‌های اجرا شده (${executionResult.stepResults.size}) با پلن (${plan.steps.size}) تطابق ندارد.")
        }

        // Check 3: Output content non-emptiness
        val hasEmptyOutputs = executionResult.stepResults.any { it.output.isBlank() }
        if (!hasEmptyOutputs) {
            passedChecks.add("خروجی تمام گام‌ها معتبر و غیرخالی است.")
        } else {
            failedChecks.add("یک یا چند گام دارای خروجی خالی هستند.")
        }

        // Check 4: Error-free step logs
        val hasStepErrors = executionResult.stepResults.any { it.error != null }
        if (!hasStepErrors) {
            passedChecks.add("گام‌ها بدون خطای داخلی اجرا شدند.")
        } else {
            failedChecks.add("خطای ثبت‌شده در لاگ گام‌های اجرایی شناسایی شد.")
        }

        val totalChecks = passedChecks.size + failedChecks.size
        val score = if (totalChecks > 0) passedChecks.size.toDouble() / totalChecks else 0.0
        val isVerified = failedChecks.isEmpty() && score >= 0.8

        return VerificationResult(
            isVerified = isVerified,
            confidenceScore = score,
            checksPassed = passedChecks,
            checksFailed = failedChecks,
            details = if (isVerified) {
                "صحت‌سنجی خروجی با ضریب اطمینان ${(score * 100).toInt()}% تایید شد."
            } else {
                "صحت‌سنجی خروجی ناموفق بود: ${failedChecks.joinToString("، ")}"
            }
        )
    }
}
