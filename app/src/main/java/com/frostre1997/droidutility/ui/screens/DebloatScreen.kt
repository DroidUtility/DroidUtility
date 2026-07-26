package com.frostre1997.droidutility.ui.screens

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frostre1997.droidutility.ShizukuShellManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AppInfo(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Long,
    val isSystemApp: Boolean,
    val isEnabled: Boolean,
    val icon: Drawable?
)

@Composable
fun DebloatScreen() {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var showSystemApps by remember { mutableStateOf(true) }
    var showEnabledApps by remember { mutableStateOf(true) }
    var showDisabledApps by remember { mutableStateOf(true) }
    var appList by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val list = installedApps.mapNotNull { app ->
                try {
                    AppInfo(
                        packageName = app.packageName,
                        appName = pm.getApplicationLabel(app).toString(),
                        versionName = pm.getPackageInfo(app.packageName, 0).versionName ?: "Unknown",
                        versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                            pm.getPackageInfo(app.packageName, 0).longVersionCode
                        } else { 0 },
                        isSystemApp = (app.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0,
                        isEnabled = app.enabled,
                        icon = app.loadIcon(pm)
                    )
                } catch (_: Exception) { null }
            }
            appList = list
            isLoading = false
        }
    }

    val filteredApps = appList.filter { app ->
        val matchesSearch = searchQuery.isBlank() ||
                app.appName.contains(searchQuery, ignoreCase = true) ||
                app.packageName.contains(searchQuery, ignoreCase = true)
        val matchesSystem = showSystemApps || !app.isSystemApp
        val matchesState = when {
            showEnabledApps && showDisabledApps -> true
            showEnabledApps -> app.isEnabled
            showDisabledApps -> !app.isEnabled
            else -> true
        }
        matchesSearch && matchesSystem && matchesState
    }

    fun performAction(app: AppInfo, action: String) {
        coroutineScope.launch {
            val command = when (action) {
                "disable" -> "pm disable ${app.packageName}"
                "enable" -> "pm enable ${app.packageName}"
                "uninstall" -> "pm uninstall ${app.packageName}"
                else -> return@launch
            }
            val result = ShizukuShellManager.executeCommand(command)
            if (result.success) {
                withContext(Dispatchers.IO) {
                    val pm = context.packageManager
                    val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                    val list = installedApps.mapNotNull { a ->
                        try {
                            AppInfo(
                                packageName = a.packageName,
                                appName = pm.getApplicationLabel(a).toString(),
                                versionName = pm.getPackageInfo(a.packageName, 0).versionName ?: "Unknown",
                                versionCode = 0,
                                isSystemApp = (a.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0,
                                isEnabled = a.enabled,
                                icon = a.loadIcon(pm)
                            )
                        } catch (_: Exception) { null }
                    }
                    appList = list
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search apps...", color = colorScheme.onSurfaceVariant) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colorScheme.onSurfaceVariant) },
            textStyle = TextStyle(color = colorScheme.onSurface),
            colors = OutlinedTextFieldDefaults.colors(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(colorScheme.surface, shape = RoundedCornerShape(16.dp))
                .border(1.dp, colorScheme.onSurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = showSystemApps,
                onClick = { showSystemApps = !showSystemApps },
                label = { Text("System", color = colorScheme.onSurface) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colorScheme.primary.copy(alpha = 0.2f),
                    selectedLabelColor = colorScheme.primary
                )
            )
            FilterChip(
                selected = showEnabledApps,
                onClick = { showEnabledApps = !showEnabledApps },
                label = { Text("Enabled", color = colorScheme.onSurface) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colorScheme.primary.copy(alpha = 0.2f),
                    selectedLabelColor = colorScheme.primary
                )
            )
            FilterChip(
                selected = showDisabledApps,
                onClick = { showDisabledApps = !showDisabledApps },
                label = { Text("Disabled", color = colorScheme.onSurface) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colorScheme.primary.copy(alpha = 0.2f),
                    selectedLabelColor = colorScheme.primary
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredApps) { app ->
                    AppItem(
                        app = app,
                        onAction = { action -> performAction(app, action) }
                    )
                }
            }
        }
    }
}

@Composable
fun AppItem(app: AppInfo, onAction: (String) -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(40.dp)) {
                if (app.icon != null) {
                    Image(
                        bitmap = app.icon.toBitmap().asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(app.appName, color = colorScheme.onSurface, fontWeight = FontWeight.Medium)
                Text(app.packageName, color = colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (app.isSystemApp) {
                        Badge(containerColor = colorScheme.primary.copy(alpha = 0.2f)) {
                            Text("System", color = colorScheme.primary, fontSize = 10.sp)
                        }
                    }
                    Badge(
                        containerColor = if (app.isEnabled) Color.Green.copy(alpha = 0.2f)
                        else Color.Red.copy(alpha = 0.2f)
                    ) {
                        Text(
                            if (app.isEnabled) "Enabled" else "Disabled",
                            color = if (app.isEnabled) Color.Green else Color.Red,
                            fontSize = 10.sp
                        )
                    }
                }
            }
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        if (expanded) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (app.isEnabled) {
                    TextButton(onClick = { onAction("disable") }) {
                        Text("Disable", color = Color.Red)
                    }
                } else {
                    TextButton(onClick = { onAction("enable") }) {
                        Text("Enable", color = Color.Green)
                    }
                }
                if (!app.isSystemApp) {
                    TextButton(onClick = { onAction("uninstall") }) {
                        Text("Uninstall", color = Color.Red)
                    }
                }
            }
        }
    }
}

fun Drawable.toBitmap(): android.graphics.Bitmap {
    val bitmap = android.graphics.Bitmap.createBitmap(
        intrinsicWidth.takeIf { it > 0 } ?: 100,
        intrinsicHeight.takeIf { it > 0 } ?: 100,
        android.graphics.Bitmap.Config.ARGB_8888
    )
    val canvas = android.graphics.Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}
