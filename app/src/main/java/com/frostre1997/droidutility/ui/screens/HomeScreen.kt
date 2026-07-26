package com.frostre1997.droidutility.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frostre1997.droidutility.ShizukuShellManager
import kotlinx.coroutines.delay

// Extension function for Toast
fun Context.toast(message: String) {
    android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    var showAboutDialog by remember { mutableStateOf(false) }

    // Shizuku state
    var isShizukuRunning by remember { mutableStateOf(false) }
    var isPermissionGranted by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // Check status periodically
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
            .padding(24.dp)
    ) {
        // Top row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Home",
                style = MaterialTheme.typography.headlineLarge,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "About",
                tint = colorScheme.onSurface,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { showAboutDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "DroidUtility – non‑root tool suite",
            color = colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Shizuku card
        Surface(
            color = colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Shizuku Manager", color = colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colorScheme.onSurfaceVariant)
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    if (isShizukuRunning) Color.Green else Color.Red,
                                    shape = RoundedCornerShape(50)
                                )
                        )
                        Text(
                            if (isShizukuRunning) "Running" else "Stopped",
                            color = colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isShizukuRunning) {
                        Text(
                            if (isPermissionGranted) "Permission: GRANTED" else "Permission: DENIED",
                            color = if (isPermissionGranted) colorScheme.onSurfaceVariant else Color.Red,
                            fontSize = 13.sp
                        )
                    } else {
                        Text("Start Shizuku first", color = Color.Yellow, fontSize = 13.sp)
                    }

                    when {
                        isShizukuRunning && !isPermissionGranted -> {
                            Button(
                                onClick = { requestPermission() },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Grant Permission", color = colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                        !isShizukuRunning -> {
                            Button(
                                onClick = {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app/")))
                                    } catch (_: Exception) { /* fallback */ }
                                },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Start Shizuku", color = colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                color = colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Total Apps", color = colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    Text("42", color = colorScheme.onSurface, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
            Surface(
                modifier = Modifier.weight(1f),
                color = colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Debloated", color = colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    Text("7", color = colorScheme.onSurface, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Recent Activity", color = colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("No recent logs", color = colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Text("Run a task or open a tool to see activity here.", color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About DroidUtility", color = colorScheme.onSurface) },
            text = {
                Column {
                    Text("Version 1.0.5-beta.6", color = colorScheme.onSurface)
                    Spacer(Modifier.height(8.dp))
                    Text("A powerful non‑root utility suite for Android.\nBuilt with 🤍 using Jetpack Compose.", color = colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK", color = colorScheme.onSurface)
                }
            },
            containerColor = colorScheme.surface
        )
    }
}
