package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.tavanacity.ui.RouterViewModel
import com.example.tavanacity.ui.TavanaCityScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: RouterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val accessibilityConfig by viewModel.accessibilityConfig.collectAsState()
            
            MyApplicationTheme(
                highContrast = accessibilityConfig.isHighContrastEnabled,
                calmMode = accessibilityConfig.isCalmModeEnabled
            ) {
                val currentDensity = LocalDensity.current
                val adjustedDensity = Density(
                    density = currentDensity.density,
                    fontScale = currentDensity.fontScale * accessibilityConfig.fontScaleFactor
                )

                CompositionLocalProvider(LocalDensity provides adjustedDensity) {
                    TavanaCityScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
