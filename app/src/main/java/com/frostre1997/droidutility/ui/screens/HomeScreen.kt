package com.frostre1997.droidutility.ui.screens

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frostre1997.droidutility.ShizukuShellManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Temporary log data class
data class LogEntry(val message: String, val timestamp: String, val status: String)

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 80.dp
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    var showAboutDialog by remember { mutableStateOf(false) }

    var isShizukuRunning by remember { mutableStateOf(false) }
    var isPermissionGranted by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    val totalApps = 42
    val debloatedApps = 7

    val recentLogs = listOf(
        LogEntry("Debloat completed", "10:30", "Success"),
        LogEntry("Terminal command executed", "10:15", "Info"),
        LogEntry("Shizuku connection lost", "09:50", "Error"),
        LogEntry("App list refreshed", "09:30", "Success"),
        LogEntry("Permission granted", "09:00", "Success")
    )

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

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = bottomPadding + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CoreStatusCard(
                    isRunning = isShizukuRunning,
                    isPermissionGranted = isPermissionGranted,
                    isLoading = isLoading,
                    onRequestPermission = { requestPermission() },
                    onStartShizuku = {
                        try {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app/"))
                            )
                        } catch (_: Exception) { /* fallback */ }
                    },
                    modifier = Modifier.weight(1f)
                )
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Apps",
                        value = totalApps.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Debloated",
                        value = debloatedApps.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            SystemInfoCard()
        }

        item {
            PermissionHealthCard(
                missingPermissionCount = 0,
                onClick = {
                    // Open a permission screen or just show a toast
                    context.toast("Permission Health Check")
                }
            )
        }

        if (recentLogs.isNotEmpty()) {
            item {
                SectionCard(
                    title = "Recent Activity",
                    onSeeAll = { /* open full log viewer */ }
                ) {
                    RecentLogsList(logs = recentLogs)
                }
            }
        }

        item {
            SectionCard(
                title = "Quick Execute"
            ) {
                QuickExecuteList(
                    items = listOf(
                        "Debloat" to Icons.Default.Build,
                        "Terminal" to Icons.Default.Terminal,
                        "Shell" to Icons.Default.Code,
                        "Settings" to Icons.Default.Settings
                    )
                ) { label ->
                    context.toast("Opening $label")
                }
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

// ---------- Helper Composables ----------

@Composable
fun CoreStatusCard(
    isRunning: Boolean,
    isPermissionGranted: Boolean,
    isLoading: Boolean,
    onRequestPermission: () -> Unit,
    onStartShizuku: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        color = colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Core", color = colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colorScheme.onSurfaceVariant)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                if (isRunning) Color.Green else Color.Red,
                                shape = RoundedCornerShape(50)
                            )
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (isRunning) "Running" else "Stopped",
                        color = colorScheme.onSurface,
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.height(4.dp))
                if (isRunning) {
                    Text(
                        if (isPermissionGranted) "Permission: GRANTED" else "Permission: DENIED",
                        color = if (isPermissionGranted) colorScheme.onSurfaceVariant else Color.Red,
                        fontSize = 12.sp
                    )
                } else {
                    Text("Start Shizuku first", color = Color.Yellow, fontSize = 12.sp)
                }
                Spacer(Modifier.height(8.dp))

                when {
                    isRunning && !isPermissionGranted -> {
                        Button(
                            onClick = onRequestPermission,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Grant", color = colorScheme.onPrimary, fontSize = 12.sp)
                        }
                    }
                    !isRunning -> {
                        Button(
                            onClick = onStartShizuku,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Start", color = colorScheme.onPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        color = colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, color = colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Text(value, color = colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SystemInfoCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    var systemInfo by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(refreshKey) {
        withContext(Dispatchers.IO) {
            val memInfo = ActivityManager.MemoryInfo()
            (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(memInfo)
            val totalMem = memInfo.totalMem / (1024 * 1024)

            val statFs = StatFs(Environment.getDataDirectory().path)
            val totalBytes = statFs.totalBytes
            val freeBytes = statFs.availableBytes

            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = if (level > 0 && scale > 0) (level * 100 / scale).toString() + "%" else "Unknown"

            val info = buildMap {
                put("Device", Build.MODEL)
                put("Manufacturer", Build.MANUFACTURER)
                put("Android", Build.VERSION.RELEASE)
                put("API", Build.VERSION.SDK_INT.toString())
                put("CPU", Build.CPU_ABI)
                put("RAM (Total)", "$totalMem MB")
                put("Storage (Total)", "${totalBytes / (1024 * 1024 * 1024)} GB")
                put("Storage (Free)", "${freeBytes / (1024 * 1024 * 1024)} GB")
                put("Battery", batteryPct)
            }
            systemInfo = info
            isLoading = false
        }
    }

    Surface(
        color = colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("System Info", color = colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = { refreshKey++ }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = colorScheme.primary)
                }
            }
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colorScheme.onSurfaceVariant)
            } else {
                systemInfo.forEach { (key, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(key, color = colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        Text(value, color = colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionHealthCard(
    missingPermissionCount: Int,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        color = colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Permission Health", color = colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text(
                    if (missingPermissionCount == 0) "All permissions granted" else "$missingPermissionCount missing",
                    color = if (missingPermissionCount == 0) Color.Green else Color.Red,
                    fontSize = 13.sp
                )
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    onSeeAll: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        color = colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                onSeeAll?.let {
                    TextButton(onClick = it) {
                        Text("See all", color = colorScheme.primary, fontSize = 13.sp)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun RecentLogsList(logs: List<LogEntry>) {
    val colorScheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        logs.forEach { log ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                when (log.status) {
                                    "Success" -> Color.Green
                                    "Error" -> Color.Red
                                    else -> Color.Yellow
                                },
                                shape = RoundedCornerShape(50)
                            )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(log.message, color = colorScheme.onSurface, fontSize = 14.sp, maxLines = 1)
                }
                Text(log.timestamp, color = colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun QuickExecuteList(
    items: List<Pair<String, androidx.compose.ui.graphics.vector.ImageVector>>,
    onItemClick: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { (label, icon) ->
            Surface(
                color = colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onItemClick(label) }
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(icon, contentDescription = null, tint = colorScheme.primary)
                    Text(label, color = colorScheme.onSurface, fontSize = 12.sp)
                }
            }
        }
    }
}

// Extension function for Toast
fun Context.toast(message: String) {
    android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
}
