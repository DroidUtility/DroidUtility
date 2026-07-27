package com.frostre1997.droidutility

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.lifecycleScope
import com.frostre1997.droidutility.data.SettingsManager
import com.frostre1997.droidutility.ui.screens.SetupScreen
import com.frostre1997.droidutility.ui.theme.DroidUtilityTheme
import com.frostre1997.droidutility.ui.theme.ThemeMode
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsManager = SettingsManager(this)

        setContent {
            // Collect settings
            val themeModeFlow = settingsManager.getThemeModeFlow().collectAsState(initial = "SYSTEM")
            val dynamicColorFlow = settingsManager.getDynamicColorFlow().collectAsState(initial = false)
            val accentColorFlow = settingsManager.getAccentColorFlow().collectAsState(initial = "blue")
            val uiScaleFlow = settingsManager.getUIScaleFlow().collectAsState(initial = 1.0f)
            val setupCompleteFlow = settingsManager.getSetupCompleteFlow().collectAsState(initial = false)

            val themeMode by themeModeFlow
            val useDynamicColor by dynamicColorFlow
            val accentColor by accentColorFlow
            val uiScale by uiScaleFlow
            val setupComplete by setupCompleteFlow

            val mode = try {
                ThemeMode.valueOf(themeMode)
            } catch (_: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }

            // UI scale
            val density = LocalDensity.current
            val scaledDensity = Density(
                density = density.density * uiScale,
                fontScale = density.fontScale * uiScale
            )

            DroidUtilityTheme(
                themeMode = mode,
                useDynamicColor = useDynamicColor,
                accentColorName = accentColor
            ) {
                CompositionLocalProvider(LocalDensity provides scaledDensity) {
                    if (!setupComplete) {
                        // First launch – show Setup screen
                        SetupScreen(
                            onSetupComplete = {
                                lifecycleScope.launch {
                                    settingsManager.setSetupComplete(true)
                                }
                            }
                        )
                    } else {
                        // Normal flow
                        Surface(modifier = Modifier.fillMaxSize()) {
                            MainScreen()
                        }
                    }
                }
            }
        }
    }
}
