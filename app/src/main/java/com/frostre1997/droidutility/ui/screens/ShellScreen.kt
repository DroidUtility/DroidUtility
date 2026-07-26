package com.frostre1997.droidutility.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frostre1997.droidutility.ShizukuShellManager
import kotlinx.coroutines.launch

@Composable
fun ShellScreen() {
    val colorScheme = MaterialTheme.colorScheme
    val coroutineScope = rememberCoroutineScope()
    var command by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }

    fun runCommand() {
        if (command.isBlank()) return
        coroutineScope.launch {
            val result = ShizukuShellManager.executeCommand(command)
            output = if (result.success) {
                result.output.ifBlank { "(no output)" }
            } else {
                "Error: ${result.error}"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Shell Command",
            style = MaterialTheme.typography.headlineMedium,
            color = colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Output
        Surface(
            color = colorScheme.surface,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 16.dp)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = output.ifBlank { "Ready – enter a shell command and press Send." },
                    color = colorScheme.onSurface,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }

        // Input row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = command,
                onValueChange = { command = it },
                placeholder = { Text("e.g., ls /sdcard", color = colorScheme.onSurfaceVariant) },
                textStyle = TextStyle(color = colorScheme.onSurface, fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            IconButton(
                onClick = { runCommand() },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = colorScheme.primary)
            }
        }
    }
}
