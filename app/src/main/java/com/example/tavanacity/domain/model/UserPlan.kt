package com.example.tavanacity.domain.model

/**
 * TAVANA Subscription & Account Plans.
 * Configured with tier boundaries, daily quotas, allowed model tiers, and Myket SKU identifiers.
 */
enum class UserPlan(
    val id: String,
    val titleFa: String,
    val priceTomans: Long,
    val dailyCredits: Int,
    val maxAllowedTier: ModelTier,
    val priorityFa: String,
    val myketSku: String,
    val featuresFa: List<String>
) {
    FREE(
        id = "free",
        titleFa = "طرح رایگان (پایه)",
        priceTomans = 0L,
        dailyCredits = 50,
        maxAllowedTier = ModelTier.ECONOMIC,
        priorityFa = "عادی",
        myketSku = "tavana_plan_free",
        featuresFa = listOf(
            "دسترسی روزانه به ۵۰ اعتبار",
            "استفاده از هوش مصنوعی اقتصادی و سریع",
            "دستیار جامع، شهر هوشمند و گفتگو",
            "پشتیبانی عمومی و مخزن گفتگوی امن"
        )
    ),
    PLUS(
        id = "plus",
        titleFa = "طرح پلاس (Plus)",
        priceTomans = 59_000L,
        dailyCredits = 300,
        maxAllowedTier = ModelTier.STANDARD,
        priorityFa = "بالا",
        myketSku = "tavana_plus_monthly",
        featuresFa = listOf(
            "۳۰۰ اعتبار روزانه (تمدید خودکار)",
            "دسترسی به متخصص فنی، برنامه‌نویسی و کدر",
            "اولویت پردازش بالاتر و زمان پاسخ‌دهی کمتر",
            "پشتیبانی از قابلیت‌های تکمیلی و چندرسانه‌ای"
        )
    ),
    PRO(
        id = "pro",
        titleFa = "طرح حرفه‌ای (Pro)",
        priceTomans = 129_000L,
        dailyCredits = 1000,
        maxAllowedTier = ModelTier.ADVANCED,
        priorityFa = "خیلی بالا",
        myketSku = "tavana_pro_monthly",
        featuresFa = listOf(
            "۱۰۰۰ اعتبار روزانه",
            "دسترسی کامل به تحلیل داده، ایده‌پرداز و منتقد منطقی",
            "اولویت پردازش بالا در سرورهای ابری",
            "پشتیبانی ویژه و بدون محدودیت پرسونا"
        )
    ),
    PRO_MAX(
        id = "pro_max",
        titleFa = "طرح پرو مکس (Pro Max)",
        priceTomans = 249_000L,
        dailyCredits = 3000,
        maxAllowedTier = ModelTier.FLAGSHIP,
        priorityFa = "حداکثر (VIP)",
        myketSku = "tavana_promax_monthly",
        featuresFa = listOf(
            "۳۰۰۰ اعتبار روزانه با بالاترین سقف مصرف",
            "دسترسی به مدل‌های پرچم‌دار و پردازش‌های سنگین",
            "بالاترین اولویت در تمامی درگاه‌ها و خوشه‌های هوش مصنوعی",
            "دسترسی زودهنگام به قابلیت‌های جدید"
        )
    );

    fun canAccessModelTier(tier: ModelTier): Boolean {
        return this.maxAllowedTier.ordinal >= tier.ordinal
    }

    companion object {
        fun fromSku(sku: String?): UserPlan {
            return entries.firstOrNull { it.myketSku.equals(sku, ignoreCase = true) } ?: FREE
        }

        fun fromId(id: String?): UserPlan {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: FREE
        }
    }
}
