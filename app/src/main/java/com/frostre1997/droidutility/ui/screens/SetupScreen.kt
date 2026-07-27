package com.frostre1997.droidutility.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frostre1997.droidutility.R
import com.frostre1997.droidutility.ShizukuShellManager
import kotlinx.coroutines.delay

@Composable
fun SetupScreen(
    onSetupComplete: () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    var isShizukuRunning by remember { mutableStateOf(false) }
    var isPermissionGranted by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            isLoading = true
            isShizukuRunning = ShizukuShellManager.checkAvailability()
            if (isShizukuRunning) {
                isPermissionGranted = ShizukuShellManager.hasPermission()
            } else {
                isPermissionGranted = false
            }
            isLoading = false
            delay(1500)
        }
    }

    fun requestPermission() {
        ShizukuShellManager.requestPermission()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ---- LOGO ----
        Image(
            painter = painterResource(id = R.drawable.play_store_512), // replace with your logo
            contentDescription = "App Logo",
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome to DroidUtility",
            style = MaterialTheme.typography.headlineLarge,
            color = colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Before you start, let's set up the app.",
            color = colorScheme.onSurfaceVariant,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Shizuku status card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Shizuku Status", color = colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colorScheme.onSurfaceVariant)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    if (isShizukuRunning) Color.Green else Color.Red,
                                    shape = RoundedCornerShape(50)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isShizukuRunning) "Running" else "Stopped",
                            color = colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isShizukuRunning) {
                        Text(
                            if (isPermissionGranted) "Permission: GRANTED" else "Permission: DENIED",
                            color = if (isPermissionGranted) colorScheme.onSurfaceVariant else Color.Red
                        )
                    } else {
                        Text("Start Shizuku first", color = Color.Yellow)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    when {
                        isShizukuRunning && !isPermissionGranted -> {
                            Button(
                                onClick = { requestPermission() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Grant Permission", color = colorScheme.onPrimary)
                            }
                        }
                        !isShizukuRunning -> {
                            Button(
                                onClick = {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app/"))
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Start Shizuku", color = colorScheme.onPrimary)
                            }
                        }
                        else -> {
                            Text("Shizuku is ready!", color = Color.Green)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val canContinue = isShizukuRunning && isPermissionGranted
        Button(
            onClick = { onSetupComplete() },
            modifier = Modifier.fillMaxWidth(),
            enabled = canContinue,
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Continue to App",
                color = if (canContinue) colorScheme.onPrimary else colorScheme.onSurfaceVariant
            )
        }
        if (!canContinue) {
            Text(
                "Please start Shizuku and grant permission to continue.",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
