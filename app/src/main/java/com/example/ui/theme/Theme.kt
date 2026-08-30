package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// High Contrast Color Scheme for accessibility / visually impaired
private val HighContrastColorScheme = darkColorScheme(
    primary = Color(0xFFFFEB3B), // High-visibility bright yellow
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF000000),
    onPrimaryContainer = Color(0xFFFFEB3B),
    secondary = Color(0xFF00E5FF),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF000000),
    onSecondaryContainer = Color(0xFF00E5FF),
    background = Color(0xFF000000),
    surface = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFFFFFFFF),
    outline = Color(0xFFFFEB3B)
)

// Calm Mode Color Scheme (آرامش اعصاب و ملایم)
private val CalmColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32), // Soft forest/sage green
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F5E9),
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary = Color(0xFF00796B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2F1),
    onSecondaryContainer = Color(0xFF004D40),
    background = Color(0xFFF1F8E9),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF263238),
    onSurface = Color(0xFF263238),
    surfaceVariant = Color(0xFFE8F5E9),
    onSurfaceVariant = Color(0xFF37474F)
)

private val DarkColorScheme = darkColorScheme(
    primary = TavanaPrimaryLight,
    onPrimary = Slate900,
    primaryContainer = TavanaPrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = TavanaSecondaryLight,
    onSecondary = Slate900,
    secondaryContainer = Color(0xFF134E4A),
    onSecondaryContainer = Color(0xFFCCFBF1),
    background = Color(0xFF090D16),
    surface = Color(0xFF0F172A),
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFFCBD5E1)
)

private val LightColorScheme = lightColorScheme(
    primary = TavanaPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = TavanaSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF134E4A),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrast: Boolean = false,
    calmMode: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        highContrast -> HighContrastColorScheme
        calmMode -> CalmColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
