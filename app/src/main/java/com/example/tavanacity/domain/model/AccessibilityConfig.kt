package com.example.tavanacity.domain.model

data class AccessibilityConfig(
    val isLargeFontEnabled: Boolean = false,
    val isHighContrastEnabled: Boolean = false,
    val isTtsAutoPlayEnabled: Boolean = false,
    val fontScaleFactor: Float = 1.0f,
    val isCalmModeEnabled: Boolean = false
)
