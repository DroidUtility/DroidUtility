package com.frostre1997.droidutility.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frostre1997.droidutility.data.SettingsManager
import com.frostre1997.droidutility.ui.theme.ThemeMode
import kotlinx.coroutines.launch

// ---------- Data classes ----------
data class SettingsGroup(
    val title: String,
    val items: List<SettingsItem>
)

sealed class SettingsItem {
    data class Switch(
        val label: String,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit
    ) : SettingsItem()

    data class SwitchWithDesc(
        val label: String,
        val description: String,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit
    ) : SettingsItem()

    data class Slider(
        val label: String,
        val value: Float,
        val range: ClosedFloatingPointRange<Float>,
        val onValueChange: (Float) -> Unit
    ) : SettingsItem()

    data class Dropdown(
        val label: String,
        val current: String,
        val options: List<String>,
        val onSelect: (String) -> Unit
    ) : SettingsItem()

    data class Button(
        val label: String,
        val onClick: () -> Unit
    ) : SettingsItem()

    data class Label(
        val text: String
    ) : SettingsItem()

    data class Action(
        val label: String,
        val onClick: () -> Unit
    ) : SettingsItem()

    data class ThemeRadioGroup(
        val currentTheme: String,
        val onThemeSelected: (String) -> Unit
    ) : SettingsItem()

    data class ColorPicker(
        val selectedColor: String,
        val onColorSelected: (String) -> Unit
    ) : SettingsItem()
}

// ---------- Helper functions for language ----------
fun languageToDisplay(code: String): String = when (code) {
    "en" -> "English"
    "es" -> "Spanish"
    "fr" -> "French"
    "de" -> "German"
    "it" -> "Italian"
    else -> "English"
}

fun displayToCode(display: String): String = when (display) {
    "English" -> "en"
    "Spanish" -> "es"
    "French" -> "fr"
    "German" -> "de"
    "Italian" -> "it"
    else -> "en"
}

// ---------- Composables ----------
@Composable
fun SettingSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onCheckedChange(!checked) }
    ) {
        Text(
            text = label,
            color = colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colorScheme.primary,
                checkedTrackColor = colorScheme.primary.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun SettingSwitchRowWithDesc(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onCheckedChange(!checked) }
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = colorScheme.onSurface)
            Text(description, color = colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colorScheme.primary,
                checkedTrackColor = colorScheme.primary.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun SettingSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(label, color = colorScheme.onSurface)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = range,
                steps = (range.endInclusive - range.start).toInt() * 2,
                colors = SliderDefaults.colors(
                    thumbColor = colorScheme.primary,
                    activeTrackColor = colorScheme.primary
                ),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Current: ${(value * 100).toInt()}%",
                color = colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun SettingDropdown(
    label: String,
    current: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { expanded = true }
    ) {
        Text(label, color = colorScheme.onSurface, modifier = Modifier.weight(1f))
        Text(current, color = colorScheme.primary)
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(colorScheme.surface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = colorScheme.onSurface) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SettingActionButton(
    label: String,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(label, color = colorScheme.onPrimary)
    }
}

@Composable
fun SettingLabel(text: String) {
    val colorScheme = MaterialTheme.colorScheme
    Text(
        text = text,
        color = colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingAction(
    label: String,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Text(label, color = colorScheme.onSurface, modifier = Modifier.weight(1f))
        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ThemeRadioGroup(
    themeMode: String,
    onThemeSelected: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        ThemeMode.values().forEach { mode ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(vertical = 4.dp)
                    .clickable { onThemeSelected(mode.name) }
            ) {
                RadioButton(
                    selected = themeMode == mode.name,
                    onClick = { onThemeSelected(mode.name) },
                    colors = RadioButtonDefaults.colors(selectedColor = colorScheme.primary)
                )
                Text(mode.name, color = colorScheme.onSurface, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
fun SettingColorPicker(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val colors = mapOf(
        "blue" to Color(0xFF4FC3F7),
        "green" to Color(0xFF66BB6A),
        "red" to Color(0xFFEF5350),
        "purple" to Color(0xFFAB47BC),
        "orange" to Color(0xFFFFA726),
        "pink" to Color(0xFFEC407A)
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text("Accent Color", color = colorScheme.onSurface)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            colors.forEach { (name, color) ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color, shape = RoundedCornerShape(50))
                        .border(
                            width = if (selectedColor == name) 3.dp else 0.dp,
                            color = colorScheme.onSurface,
                            shape = RoundedCornerShape(50)
                        )
                        .clickable { onColorSelected(name) }
                )
            }
        }
    }
}

// ---------- Main Settings Screen ----------
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    val colorScheme = MaterialTheme.colorScheme

    // States
    val themeMode by settingsManager.getThemeModeFlow().collectAsState(initial = "SYSTEM")
    val dynamicColor by settingsManager.getDynamicColorFlow().collectAsState(initial = false)
    val terminalFontSize by settingsManager.getTerminalFontSizeFlow().collectAsState(initial = 16f)
    val terminalFontFamily by settingsManager.getTerminalFontFamilyFlow().collectAsState(initial = "monospace")
    val customFontPath by settingsManager.getCustomFontPathFlow().collectAsState(initial = "")
    val experimentalFeatureX by settingsManager.getExperimentalFeatureXFlow().collectAsState(initial = false)
    val experimentalFeatureY by settingsManager.getExperimentalFeatureYFlow().collectAsState(initial = false)
    val recordLogs by settingsManager.getRecordLogsFlow().collectAsState(initial = false)
    val enableTelemetry by settingsManager.getEnableTelemetryFlow().collectAsState(initial = false)
    val enableCrashReports by settingsManager.getEnableCrashReportsFlow().collectAsState(initial = false)
    val language by settingsManager.getLanguageFlow().collectAsState(initial = "en")
    val colorfulWorkflowCards by settingsManager.getColorfulWorkflowCardsFlow().collectAsState(initial = false)
    val uiScale by settingsManager.getUIScaleFlow().collectAsState(initial = 1.0f)
    val accentColor by settingsManager.getAccentColorFlow().collectAsState(initial = "blue")

    // File picker
    val fontPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                settingsManager.setCustomFontPath(it.toString())
            }
        }
    }

    // Build groups
    val groups = buildList {
        // Language
        add(
            SettingsGroup(
                title = "Language",
                items = listOf(
                    SettingsItem.Dropdown(
                        "Language",
                        languageToDisplay(language),
                        listOf("English", "Spanish", "French", "German", "Italian")
                    ) { lang ->
                        coroutineScope.launch {
                            settingsManager.setLanguage(displayToCode(lang))
                        }
                    },
                    SettingsItem.Label("Choose app display language")
                )
            )
        )

        // Appearance
        add(
            SettingsGroup(
                title = "Appearance",
                items = listOf(
                    SettingsItem.ThemeRadioGroup(
                        themeMode,
                        onThemeSelected = { selected ->
                            coroutineScope.launch { settingsManager.setThemeMode(selected) }
                        }
                    ),
                    SettingsItem.Switch("Dynamic Color", dynamicColor) {
                        coroutineScope.launch { settingsManager.setDynamicColor(it) }
                    },
                    SettingsItem.Switch("Colorful workflow cards (Beta)", colorfulWorkflowCards) {
                        coroutineScope.launch { settingsManager.setColorfulWorkflowCards(it) }
                    },
                    // Liquid Glass removed
                    SettingsItem.Slider("Scale", uiScale, 0.5f..1.5f) { newScale ->
                        coroutineScope.launch { settingsManager.setUIScale(newScale) }
                    },
                    SettingsItem.ColorPicker(
                        selectedColor = accentColor,
                        onColorSelected = { color ->
                            coroutineScope.launch {
                                settingsManager.setAccentColor(color)
                            }
                        }
                    )
                )
            )
        )

        // Terminal
        add(
            SettingsGroup(
                title = "Terminal",
                items = listOf(
                    SettingsItem.Slider("Font Size", terminalFontSize, 10f..30f) { newSize ->
                        coroutineScope.launch { settingsManager.setTerminalFontSize(newSize) }
                    },
                    SettingsItem.Dropdown(
                        "Font Family",
                        terminalFontFamily,
                        listOf("monospace", "sans-serif", "serif", "sans-serif-condensed")
                    ) { family ->
                        coroutineScope.launch { settingsManager.setTerminalFontFamily(family) }
                    }
                )
            )
        )

        // Font Management
        add(
            SettingsGroup(
                title = "Font Management",
                items = buildList {
                    add(
                        SettingsItem.Button("Upload Custom Font (.ttf)") {
                            fontPickerLauncher.launch("application/octet-stream")
                        }
                    )
                    if (customFontPath.isNotEmpty()) {
                        add(SettingsItem.Label("Current: $customFontPath"))
                    }
                }
            )
        )

        // Experimental
        add(
            SettingsGroup(
                title = "Experimental",
                items = listOf(
                    SettingsItem.SwitchWithDesc(
                        "Shizuku ADB Fallback",
                        "Fall back to ADB over Wi‑Fi if Shizuku is unavailable (debug builds only)",
                        experimentalFeatureX
                    ) {
                        coroutineScope.launch { settingsManager.setExperimentalFeatureX(it) }
                    },
                    SettingsItem.SwitchWithDesc(
                        "Force Dynamic Color",
                        "Apply Material You theming even on Android 10 and below (experimental)",
                        experimentalFeatureY
                    ) {
                        coroutineScope.launch { settingsManager.setExperimentalFeatureY(it) }
                    }
                )
            )
        )

        // Debugging
        add(
            SettingsGroup(
                title = "Debugging",
                items = listOf(
                    SettingsItem.Switch("Record Debug Logs", recordLogs) {
                        coroutineScope.launch { settingsManager.setRecordLogs(it) }
                    },
                    SettingsItem.Switch("Enable Telemetry", enableTelemetry) {
                        coroutineScope.launch { settingsManager.setEnableTelemetry(it) }
                    },
                    SettingsItem.Switch("Enable Crash Reports", enableCrashReports) {
                        coroutineScope.launch { settingsManager.setEnableCrashReports(it) }
                    }
                )
            )
        )

        // General Settings
        add(
            SettingsGroup(
                title = "General Settings",
                items = listOf(
                    SettingsItem.Action("Module Config") { /* navigate */ },
                    SettingsItem.Action("Global Variables") { /* navigate */ },
                    SettingsItem.Action("Model Config") { /* navigate */ }
                )
            )
        )

        // About
        add(
            SettingsGroup(
                title = "About",
                items = listOf(
                    SettingsItem.Label("Version 1.0.5-beta.6"),
                    SettingsItem.Label("Built with 🤍 using Jetpack Compose"),
                    SettingsItem.Action("View Open Source Licenses") { /* open licenses */ },
                    SettingsItem.Action("Developer Info") { /* open developer info */ }
                )
            )
        )
    }

    // Filter groups
    val filteredGroups = if (searchQuery.isBlank()) {
        groups
    } else {
        groups.mapNotNull { group ->
            val filteredItems = group.items.filter { item ->
                when (item) {
                    is SettingsItem.Switch -> item.label.contains(searchQuery, ignoreCase = true)
                    is SettingsItem.SwitchWithDesc -> item.label.contains(searchQuery, ignoreCase = true) || item.description.contains(searchQuery, ignoreCase = true)
                    is SettingsItem.Slider -> item.label.contains(searchQuery, ignoreCase = true)
                    is SettingsItem.Dropdown -> item.label.contains(searchQuery, ignoreCase = true) || item.current.contains(searchQuery, ignoreCase = true) || item.current.contains(searchQuery, ignoreCase = true)
                    is SettingsItem.Button -> item.label.contains(searchQuery, ignoreCase = true)
                    is SettingsItem.Label -> item.text.contains(searchQuery, ignoreCase = true)
                    is SettingsItem.Action -> item.label.contains(searchQuery, ignoreCase = true)
                    is SettingsItem.ThemeRadioGroup -> true
                    is SettingsItem.ColorPicker -> true
                    else -> false
                }
            }
            if (filteredItems.isNotEmpty()) {
                group.copy(items = filteredItems)
            } else null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { 
                Text(
                     text = "Search settings", 
                     color = colorScheme.onSurfaceVariant
                ) 
            },
            leadingIcon = { 
                Icon(
                    Icons.Default.Search, 
                    contentDescription = null, 
                    tint = colorScheme.onSurfaceVariant
                ) 
            },
            textStyle = TextStyle(color = colorScheme.onSurface),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorScheme.primary,
                unfocusedBorderColor = colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                cursorColor = colorScheme.primary,
                containerColor = colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(colorScheme.surface, shape = RoundedCornerShape(16.dp))
                .border(
                    1.dp, 
                    colorScheme.onSurfaceVariant.copy(alpha = 0.3f), 
                    RoundedCornerShape(16.dp))
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(filteredGroups) { group ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Group title (inside the card)
                        Text(
                            text = group.title,
                            color = colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        )

                        group.items.forEachIndexed { index, item ->
                            when (item) {
                                is SettingsItem.Switch -> {
                                    SettingSwitchRow(
                                        label = item.label,
                                        checked = item.checked,
                                        onCheckedChange = item.onCheckedChange
                                    )
                                }
                                is SettingsItem.SwitchWithDesc -> {
                                    SettingSwitchRowWithDesc(
                                        label = item.label,
                                        description = item.description,
                                        checked = item.checked,
                                        onCheckedChange = item.onCheckedChange
                                    )
                                }
                                is SettingsItem.Slider -> {
                                    SettingSlider(
                                        label = item.label,
                                        value = item.value,
                                        range = item.range,
                                        onValueChange = item.onValueChange
                                    )
                                }
                                is SettingsItem.Dropdown -> {
                                    SettingDropdown(
                                        label = item.label,
                                        current = item.current,
                                        options = item.options,
                                        onSelect = item.onSelect
                                    )
                                }
                                is SettingsItem.Button -> {
                                    SettingActionButton(
                                        label = item.label,
                                        onClick = item.onClick
                                    )
                                }
                                is SettingsItem.Label -> {
                                    SettingLabel(text = item.text)
                                }
                                is SettingsItem.Action -> {
                                    SettingAction(
                                        label = item.label,
                                        onClick = item.onClick
                                    )
                                }
                                is SettingsItem.ThemeRadioGroup -> {
                                    ThemeRadioGroup(
                                        themeMode = item.currentTheme,
                                        onThemeSelected = item.onThemeSelected
                                    )
                                }
                                is SettingsItem.ColorPicker -> {
                                    SettingColorPicker(
                                        selectedColor = item.selectedColor,
                                        onColorSelected = item.onColorSelected
                                    )
                                }
                            }
                            if (index < group.items.size - 1) {
                                Divider(
                                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
