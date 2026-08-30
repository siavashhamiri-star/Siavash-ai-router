package com.example.tavanacity.domain.model

sealed class RouterError(
    val userMessageFa: String,
    val technicalLog: String
) : Exception(technicalLog) {

    class NetworkError(
        technicalLog: String = "Network is unreachable or connection dropped",
        userMessageFa: String = "ارتباط با شبکه برقرار نشد. لطفاً اتصال اینترنت خود را بررسی کنید."
    ) : RouterError(userMessageFa, technicalLog)

    class AuthenticationError(
        technicalLog: String = "Missing or invalid API authentication credentials",
        userMessageFa: String = "خطای احراز هویت سرویس هوش مصنوعی. کلید دسترسی پیکربندی نشده است."
    ) : RouterError(userMessageFa, technicalLog)

    class TimeoutError(
        technicalLog: String = "AI provider request timed out",
        userMessageFa: String = "زمان انتظار برای دریافت پاسخ از سرویس هوش مصنوعی به پایان رسید."
    ) : RouterError(userMessageFa, technicalLog)

    class ProviderError(
        val httpCode: Int = 0,
        technicalLog: String = "AI provider returned server failure (HTTP $httpCode)",
        userMessageFa: String = "سرویس‌دهنده هوش مصنوعی با خطا مواجه شد. لطفاً مجدداً تلاش فرمایید."
    ) : RouterError(userMessageFa, technicalLog)

    class AIUnavailable(
        technicalLog: String = "AI services are currently unavailable or offline without real fallback",
        userMessageFa: String = "سرویس هوش مصنوعی در حال حاضر در دسترس نیست."
    ) : RouterError(userMessageFa, technicalLog)

    class SafetyBlocked(
        val category: String = "General Safety",
        technicalLog: String = "Content violates system safety policies (Category: $category)",
        userMessageFa: String = "این پیام به دلیل مغایرت با پروتکل‌های ایمنی سیستم مسدود شد."
    ) : RouterError(userMessageFa, technicalLog)

    class InsufficientCredit(
        val requiredCredits: Int,
        val currentBalance: Int,
        technicalLog: String = "User credit ($currentBalance) is insufficient for required ($requiredCredits)",
        userMessageFa: String = "اعتبار حساب شما برای ارسال این پیام کافی نیست ($currentBalance از $requiredCredits سکه). لطفاً اشتراک خود را ارتقا دهید یا اعتبار تهیه فرمایید."
    ) : RouterError(userMessageFa, technicalLog)

    class EntitlementRequired(
        val requiredPlan: UserPlan,
        technicalLog: String = "Plan ${requiredPlan.name} is required to access this AI tier",
        userMessageFa: String = "برای دسترسی به این پرسونا و مدل هوش مصنوعی، نیاز به اشتراک ${requiredPlan.titleFa} دارید."
    ) : RouterError(userMessageFa, technicalLog)

    class UnknownError(
        technicalLog: String = "Unexpected error occurred during routing",
        userMessageFa: String = "خطای پیش‌بینی‌نشده در پردازش پیام رخ داد."
    ) : RouterError(userMessageFa, technicalLog)
}
