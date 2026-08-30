package com.example.tavanacity.data.safety

import android.util.Log

data class SafetyResult(
    val isPassed: Boolean,
    val category: String? = null,
    val reasonFa: String? = null
) {
    companion object {
        fun passed() = SafetyResult(isPassed = true)
        fun blocked(category: String, reasonFa: String) = SafetyResult(
            isPassed = false,
            category = category,
            reasonFa = reasonFa
        )
    }
}

interface SafetyRule {
    val id: String
    val category: String
    val reasonFa: String
    fun isViolated(content: String): Boolean
}

class RegexSafetyRule(
    override val id: String,
    override val category: String,
    override val reasonFa: String,
    private val pattern: Regex
) : SafetyRule {
    override fun isViolated(content: String): Boolean {
        return pattern.containsMatchIn(content)
    }
}

class TavanaCitySafetyLayer(
    initialRules: List<SafetyRule> = defaultRules
) {
    private val rules: MutableList<SafetyRule> = initialRules.toMutableList()

    fun addRule(rule: SafetyRule) {
        rules.add(rule)
    }

    fun validateInput(input: String): SafetyResult {
        return try {
            val normalized = input.trim()
            if (normalized.isBlank()) {
                return SafetyResult.blocked(
                    category = "EmptyInput",
                    reasonFa = "پیام نمی‌تواند خالی باشد."
                )
            }

            for (rule in rules) {
                if (rule.isViolated(normalized)) {
                    // Note: Do NOT log the actual user content for privacy and security
                    Log.w("TavanaSafety", "Safety policy triggered for rule ID: ${rule.id}")
                    return SafetyResult.blocked(
                        category = rule.category,
                        reasonFa = rule.reasonFa
                    )
                }
            }
            SafetyResult.passed()
        } catch (e: Exception) {
            // Fail-safe handling: log error without sensitive data
            Log.e("TavanaSafety", "Error in safety evaluation engine: ${e.javaClass.simpleName}")
            SafetyResult.passed()
        }
    }

    companion object {
        val defaultRules: List<SafetyRule> = listOf(
            RegexSafetyRule(
                id = "system_override_jailbreak",
                category = "Prompt Injection / Security",
                reasonFa = "درخواست شما حاوی الگوهای تغییر غیرمجاز دستورالعمل‌های هسته سیستم است.",
                pattern = Regex(
                    "(?i)(ignore (all )?previous (instructions|rules|directives)|system prompt reveal|disregard all (rules|instructions)|bypass safety)"
                )
            ),
            RegexSafetyRule(
                id = "dangerous_content_detection",
                category = "Harmful Content / Safety",
                reasonFa = "درخواست شما به دلیل مغایرت با قوانین ایمنی و محتوای پرخطر مسدود شد.",
                pattern = Regex(
                    "(?i)(how to build a (bomb|weapon)|create explosive)"
                )
            ),
            RegexSafetyRule(
                id = "malicious_payload_detection",
                category = "Malicious Payload",
                reasonFa = "درخواست حاوی کدهای مخرب یا الگوهای تزریق ساختاریافته است.",
                pattern = Regex(
                    "(?i)(<script.*?>|DROP\\s+TABLE|rm\\s+-rf\\s+/|exec\\s+xp_cmdshell)"
                )
            )
        )
    }
}
