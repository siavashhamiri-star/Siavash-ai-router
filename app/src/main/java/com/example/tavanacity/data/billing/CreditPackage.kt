package com.example.tavanacity.data.billing

/**
 * Consumable credit top-ups available in Myket store.
 */
enum class CreditPackage(
    val sku: String,
    val titleFa: String,
    val credits: Int,
    val priceTomans: Long,
    val badgeFa: String? = null
) {
    PACK_200(
        sku = "tavana_pack_200",
        titleFa = "بسته ۲۰۰ اعتبار",
        credits = 200,
        priceTomans = 25_000L
    ),
    PACK_1000(
        sku = "tavana_pack_1000",
        titleFa = "بسته ۱,۰۰۰ اعتبار",
        credits = 1000,
        priceTomans = 89_000L,
        badgeFa = "محبوب‌ترین"
    ),
    PACK_3000(
        sku = "tavana_pack_3000",
        titleFa = "بسته ۳,۰۰۰ اعتبار",
        credits = 3000,
        priceTomans = 199_000L,
        badgeFa = "بهترین ارزش"
    );

    companion object {
        fun fromSku(sku: String?): CreditPackage? {
            return entries.firstOrNull { it.sku.equals(sku, ignoreCase = true) }
        }
    }
}
