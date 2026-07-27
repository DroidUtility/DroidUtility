package com.frostre1997.droidutility

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.frostre1997.droidutility.data.SettingsManager
import com.frostre1997.droidutility.ui.theme.DroidUtilityTheme
import com.frostre1997.droidutility.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    private lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsManager = SettingsManager(this)

        setContent {
            val themeModeFlow = settingsManager.getThemeModeFlow().collectAsState(initial = "SYSTEM")
            val dynamicColorFlow = settingsManager.getDynamicColorFlow().collectAsState(initial = false)
            val accentColorFlow = settingsManager.getAccentColorFlow().collectAsState(initial = "blue")
            val uiScaleFlow = settingsManager.getUIScaleFlow().collectAsState(initial = 1.0f)

            val themeMode by themeModeFlow
            val useDynamicColor by dynamicColorFlow
            val accentColor by accentColorFlow
            val uiScale by uiScaleFlow

            val mode = try {
                ThemeMode.valueOf(themeMode)
            } catch (_: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }

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
                    Surface(modifier = Modifier.fillMaxSize()) {
                        MainScreen(settingsManager = settingsManager)
                    }
                }
            }
        }
    }
}
