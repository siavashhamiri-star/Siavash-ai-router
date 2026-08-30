package com.example.tavanacity.domain.model

enum class EntitlementStatus(val titleFa: String) {
    FREE("رایگان"),
    ACTIVE("فعال"),
    EXPIRED("منقضی شده"),
    SUSPENDED("معلق"),
    GRACE_PERIOD("مهلت تمدید")
}
