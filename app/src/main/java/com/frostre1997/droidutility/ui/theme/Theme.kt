package com.frostre1997.droidutility.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Predefined accent colors
val accentColors = mapOf(
    "blue" to Color(0xFF4FC3F7),
    "green" to Color(0xFF66BB6A),
    "red" to Color(0xFFEF5350),
    "purple" to Color(0xFFAB47BC),
    "orange" to Color(0xFFFFA726),
    "pink" to Color(0xFFEC407A)
)

// Default accent (fallback)
private val defaultAccent = accentColors["blue"]!!

// Color schemes – accent color is parameterized
private fun getColorScheme(
    accentColor: Color,
    isDark: Boolean,
    isAmoled: Boolean = false
): ColorScheme {
    val background = when {
        isAmoled -> Color.Black
        isDark -> Color(0xFF121212)
        else -> Color.White
    }
    val surface = when {
        isAmoled -> Color.Black
        isDark -> Color(0xFF1E1E1E)
        else -> Color.White
    }
    val onBackground = if (isDark || isAmoled) Color.White else Color.Black
    val onSurface = if (isDark || isAmoled) Color.White else Color.Black

    return if (isDark || isAmoled) {
        darkColorScheme(
            primary = accentColor,
            secondary = accentColor.copy(alpha = 0.7f),
            tertiary = accentColor.copy(alpha = 0.5f),
            background = background,
            surface = surface,
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onTertiary = Color.Black,
            onBackground = onBackground,
            onSurface = onSurface
        )
    } else {
        lightColorScheme(
            primary = accentColor,
            secondary = accentColor.copy(alpha = 0.7f),
            tertiary = accentColor.copy(alpha = 0.5f),
            background = background,
            surface = surface,
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onTertiary = Color.Black,
            onBackground = onBackground,
            onSurface = onSurface
        )
    }
}

enum class ThemeMode {
    LIGHT, DARK, AMOLED, SYSTEM
}

@Composable
fun DroidUtilityTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    useDynamicColor: Boolean = false,
    accentColorName: String = "blue",
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val accentColor = accentColors[accentColorName] ?: defaultAccent

    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (themeMode == ThemeMode.LIGHT || (themeMode == ThemeMode.SYSTEM && !isDark)) {
                dynamicLightColorScheme(context)
            } else {
                dynamicDarkColorScheme(context)
            }
        }
        else -> {
            val dark = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.AMOLED -> true
                else -> isDark // SYSTEM
            }
            val amoled = themeMode == ThemeMode.AMOLED
            getColorScheme(accentColor, dark, amoled)
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()

            val controller = WindowCompat.getInsetsController(window, view)
            val isLightTheme = (themeMode == ThemeMode.LIGHT) || (themeMode == ThemeMode.SYSTEM && !isDark)
            controller.isAppearanceLightStatusBars = isLightTheme
            controller.isAppearanceLightNavigationBars = isLightTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
