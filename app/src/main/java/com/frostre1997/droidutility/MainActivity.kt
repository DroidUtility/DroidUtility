package com.frostre1997.droidutility

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.frostre1997.droidutility.data.SettingsManager
import com.frostre1997.droidutility.ui.screens.SetupScreen
import com.frostre1997.droidutility.ui.theme.DroidUtilityTheme
import com.frostre1997.droidutility.ui.theme.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

            val density = LocalDensity.current
            val scaledDensity = androidx.compose.ui.unit.Density(
                density = density.density * uiScale,
                fontScale = density.fontScale * uiScale
            )

            var showSplash by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                delay(1500)
                showSplash = false
            }

            DroidUtilityTheme(
                themeMode = mode,
                useDynamicColor = useDynamicColor,
                accentColorName = accentColor
            ) {
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.ui.platform.LocalDensity provides scaledDensity
                ) {
                    if (showSplash) {
                        SplashScreen()
                    } else {
                        if (!setupComplete) {
                            SetupScreen(
                                onSetupComplete = {
                                    lifecycleScope.launch {
                                        settingsManager.setSetupComplete(true)
                                    }
                                }
                            )
                        } else {
                            Surface(modifier = Modifier.fillMaxSize()) {
                                MainScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Using your monochrome icon
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_monochrome),
            contentDescription = "App Logo",
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "DroidUtility",
            style = MaterialTheme.typography.headlineLarge,
            color = colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "v1.0.5-beta.6",
            color = colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        CircularProgressIndicator(
            color = colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
    }
}
