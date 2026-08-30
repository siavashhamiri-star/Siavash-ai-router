package com.example.tavanacity.domain.model

/**
 * Defines computational and architectural tiers for AI models.
 * Used for economic safety and credit deduction per request.
 */
enum class ModelTier(
    val id: String,
    val titleFa: String,
    val creditCost: Int
) {
    ECONOMIC(
        id = "economic",
        titleFa = "اقتصادی و سریع",
        creditCost = 1
    ),
    STANDARD(
        id = "standard",
        titleFa = "استاندارد",
        creditCost = 3
    ),
    ADVANCED(
        id = "advanced",
        titleFa = "پیشرفته و تحلیلی",
        creditCost = 8
    ),
    FLAGSHIP(
        id = "flagship",
        titleFa = "پرچم‌دار و فوق‌پیشرفته",
        creditCost = 15
    )
}
