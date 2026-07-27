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

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    var showAboutDialog by remember { mutableStateOf(false) }

    var isShizukuRunning by remember { mutableStateOf(false) }
    var isPermissionGranted by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            isLoading = true
            isShizukuRunning = ShizukuShellManager.checkAvailability()
            isPermissionGranted = if (isShizukuRunning) ShizukuShellManager.hasPermission() else false
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
                modifier = Modifier.size(28.dp).clickable { showAboutDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "DroidUtility – non-root tool suite",
            color = colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Shizuku Manager", color = colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))

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
                    Spacer(modifier = Modifier.height(12.dp))

                    when {
                        isShizukuRunning && !isPermissionGranted -> {
                            Button(
                                onClick = { requestPermission() },
                                modifier = Modifier.fillMaxWidth(),
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
                                        context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("https://shizuku.rikka.app/")
                                            )
                                        )
                                    } catch (_: Exception) { /* fallback */ }
                                },
                                modifier = Modifier.fillMaxWidth(),
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(title = "Total Apps", value = "42")
            StatCard(title = "Debloated", value = "7")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Recent Activity", color = colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("No recent logs", color = colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Text(
                    text = "Run a task or open a tool to see activity here.",
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
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
                    Text(
                        text = "A powerful non-root utility suite for Android. Built with 🤍 using Jetpack Compose.",
                        color = colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("OK", color = colorScheme.onSurface) }
            },
            containerColor = colorScheme.surface
        )
    }
}

@Composable
fun StatCard(title: String, value: String) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Text(value, color = colorScheme.onSurface, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}
