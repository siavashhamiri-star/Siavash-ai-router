package com.example.tavanacity.domain.architect.judge

import com.example.tavanacity.domain.architect.model.ArchitectTask
import com.example.tavanacity.domain.architect.model.ExecutionPlan
import com.example.tavanacity.domain.architect.model.ExecutionResult
import com.example.tavanacity.domain.architect.model.JudgeVerdict
import com.example.tavanacity.domain.architect.model.VerificationResult

/**
 * Interface for evaluating the overall quality, completeness, and architectural soundness
 * of executed tasks and plans.
 */
interface JudgeInterface {
    suspend fun evaluate(
        task: ArchitectTask,
        plan: ExecutionPlan,
        executionResult: ExecutionResult,
        verificationResult: VerificationResult
    ): JudgeVerdict
}

/**
 * Default Architect Judge implementation providing structured qualitative scoring
 * and actionable recommendations.
 */
class DefaultArchitectJudge : JudgeInterface {

    override suspend fun evaluate(
        task: ArchitectTask,
        plan: ExecutionPlan,
        executionResult: ExecutionResult,
        verificationResult: VerificationResult
    ): JudgeVerdict {
        val feedback = mutableListOf<String>()
        var score = 100

        if (!executionResult.isSuccess) {
            score -= 50
            feedback.add("اجرا با خطا همراه بوده است.")
        }

        if (!verificationResult.isVerified) {
            score -= 30
            feedback.add("صحت‌سنجی خروجی کامل نیست: ${verificationResult.details}")
        } else {
            feedback.add("صحت‌سنجی خروجی با موفقیت تایید شد.")
        }

        if (plan.steps.isEmpty()) {
            score -= 20
            feedback.add("پلن بدون گام اجرایی بوده است.")
        }

        score = score.coerceIn(0, 100)
        val isApproved = score >= 70 && executionResult.isSuccess && verificationResult.isVerified

        val summary = if (isApproved) {
            "ماموریت معماری '${task.title}' با امتیاز کیفی $score از ۱۰۰ با موفقیت داوری و تایید شد."
        } else {
            "ماموریت معماری '${task.title}' به علت عدم احراز معیارهای کیفی (امتیاز $score) تایید نشد."
        }

        return JudgeVerdict(
            isApproved = isApproved,
            score = score,
            summary = summary,
            feedback = feedback,
            completedAt = System.currentTimeMillis()
        )
    }
}
