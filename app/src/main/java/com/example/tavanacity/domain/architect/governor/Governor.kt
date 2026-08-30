package com.example.tavanacity.domain.architect.governor

import com.example.tavanacity.domain.architect.model.ArchitectTask
import com.example.tavanacity.domain.architect.model.ExecutionPlan
import com.example.tavanacity.domain.architect.model.SecurityLevel

/**
 * Decision outcome from the Governor.
 */
enum class GovernorDecision {
    ALLOWED,
    BLOCKED,
    CONDITIONAL_APPROVAL
}

/**
 * Structured evaluation result from the Governor.
 */
data class GovernorEvaluation(
    val decision: GovernorDecision,
    val reason: String,
    val securityLevel: SecurityLevel,
    val requiredModifications: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Governor interface responsible for enforcing safety, budget, policy constraints,
 * and pre-execution authorization.
 */
interface Governor {
    suspend fun evaluateTask(task: ArchitectTask): GovernorEvaluation
    suspend fun evaluatePlan(task: ArchitectTask, plan: ExecutionPlan): GovernorEvaluation
}

/**
 * Default Governor implementation enforcing safety constraints and preventing unauthorized actions.
 */
class DefaultGovernor(
    private val maxAllowedStepsPerPlan: Int = 10,
    private val allowedActionTypes: Set<String> = setOf(
        "MOCK_CODE_ANALYSIS",
        "MOCK_FILE_READ",
        "MOCK_FILE_WRITE",
        "MOCK_TEST_RUN",
        "MOCK_DIAGNOSTIC",
        "MOCK_SYNTHESIS"
    )
) : Governor {

    private val forbiddenKeywords = listOf(
        "DROP_TABLE",
        "EXEC_ROOT",
        "SELF_MUTATE_CORE",
        "BYPASS_SECURITY",
        "PURGE_ALL_DATA"
    )

    override suspend fun evaluateTask(task: ArchitectTask): GovernorEvaluation {
        val rawContent = "${task.title} ${task.description}".uppercase()

        for (forbidden in forbiddenKeywords) {
            if (rawContent.contains(forbidden)) {
                return GovernorEvaluation(
                    decision = GovernorDecision.BLOCKED,
                    reason = "وظیفه درخواستی حاوی کلمات کلیدی مسدودشده امنیتی است: $forbidden",
                    securityLevel = SecurityLevel.HIGH_RISK
                )
            }
        }

        return GovernorEvaluation(
            decision = GovernorDecision.ALLOWED,
            reason = "وظیفه با سیاست‌های نظارتی توانا سازگار است.",
            securityLevel = SecurityLevel.SAFE
        )
    }

    override suspend fun evaluatePlan(task: ArchitectTask, plan: ExecutionPlan): GovernorEvaluation {
        if (plan.steps.isEmpty()) {
            return GovernorEvaluation(
                decision = GovernorDecision.BLOCKED,
                reason = "پلن اجرایی نمی‌تواند فاقد هرگونه گام اجرایی باشد.",
                securityLevel = SecurityLevel.SAFE
            )
        }

        if (plan.steps.size > maxAllowedStepsPerPlan) {
            return GovernorEvaluation(
                decision = GovernorDecision.BLOCKED,
                reason = "تعداد گام‌های پلن (${plan.steps.size}) بیش از حد مجاز ($maxAllowedStepsPerPlan) است.",
                securityLevel = SecurityLevel.RESTRICTED
            )
        }

        // Validate individual action types
        for (step in plan.steps) {
            if (step.actionType !in allowedActionTypes) {
                return GovernorEvaluation(
                    decision = GovernorDecision.BLOCKED,
                    reason = "نوع عملیات '${step.actionType}' در سطح پایه معماری مجاز نیست.",
                    securityLevel = SecurityLevel.HIGH_RISK
                )
            }
        }

        return GovernorEvaluation(
            decision = GovernorDecision.ALLOWED,
            reason = "پلن اجرایی تایید شد و آماده اجرا در درگاه است.",
            securityLevel = plan.securityLevel
        )
    }
}
