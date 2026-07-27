package com.frostre1997.droidutility

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.frostre1997.droidutility.data.SettingsManager
import com.frostre1997.droidutility.navigation.FloatingBottomBar
import com.frostre1997.droidutility.ui.screens.*

@Composable
fun MainScreen(
    settingsManager: SettingsManager
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: Screen.Home.route

    // Read Liquid Glass setting – default to true so bar is shown
    val liquidGlassEnabled by settingsManager.getLiquidGlassNavFlow().collectAsState(initial = true)

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (liquidGlassEnabled) 80.dp else 0.dp)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Terminal.route) { TerminalScreen() }
            composable(Screen.Debloat.route) { DebloatScreen() }
            composable(Screen.Shell.route) { ShellScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }

        // Only show floating bar if enabled
        if (liquidGlassEnabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                FloatingBottomBar(
                    currentRoute = currentRoute,
                    onItemClick = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}
