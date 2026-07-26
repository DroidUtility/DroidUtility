package com.frostre1997.droidutility.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frostre1997.droidutility.ShizukuShellManager
import kotlinx.coroutines.launch

@Composable
fun TerminalScreen() {
    val colorScheme = MaterialTheme.colorScheme
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    val outputLines = remember { mutableStateListOf<String>() }
    val scrollState = rememberLazyListState()

    fun executeCommand(command: String) {
        if (command.isBlank()) return
        outputLines.add("$ ${command}")
        coroutineScope.launch {
            val result = ShizukuShellManager.executeCommand(command)
            if (result.success) {
                outputLines.add(result.output.ifBlank { "(no output)" })
            } else {
                outputLines.add("Error: ${result.error}")
            }
            // Scroll to bottom
            scrollState.animateScrollToItem(outputLines.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(16.dp)
    ) {
        // Output
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(colorScheme.surface, RoundedCornerShape(8.dp))
                .padding(8.dp),
            state = scrollState,
            reverseLayout = false
        ) {
            items(outputLines) { line ->
                Text(
                    text = line,
                    color = if (line.startsWith("$")) colorScheme.primary else colorScheme.onSurface,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            if (outputLines.isEmpty()) {
                item {
                    Text(
                        text = "Ready. Enter a command to execute via Shizuku.",
                        color = colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Enter command...", color = colorScheme.onSurfaceVariant) },
                textStyle = TextStyle(color = colorScheme.onSurface, fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        executeCommand(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = colorScheme.primary)
            }
        }
    }
}
