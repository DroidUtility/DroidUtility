package com.frostre1997.droidutility.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frostre1997.droidutility.core.ShellManager
import kotlinx.coroutines.launch

fun Context.toast(message: String) {
    android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
}

data class ShellLine(val text: String, val isCommand: Boolean)

@Composable
fun ShellScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    val outputLines = remember { mutableStateListOf<ShellLine>() }
    var history by remember { mutableStateOf(listOf<String>()) }
    var historyIndex by remember { mutableStateOf(-1) }

    fun executeCommand(command: String) {
        if (command.isBlank()) return
        outputLines.add(ShellLine("% $command", isCommand = true))
        history = history + command
        historyIndex = history.size

        coroutineScope.launch {
            val result = ShellManager.executeCommand(command)
            if (result.success) {
                val output = result.output
                if (output.isNotBlank()) {
                    outputLines.addAll(output.lines().map { ShellLine(it, isCommand = false) })
                } else {
                    outputLines.add(ShellLine("(no output)", isCommand = false))
                }
            } else {
                val error = result.error.ifBlank { "Command failed (exit code: ${result.exitCode})" }
                outputLines.addAll(error.lines().map { ShellLine("ERROR: $it", isCommand = false) })
            }
        }
        inputText = ""
    }

    fun handleKeyEvent(event: androidx.compose.ui.input.key.KeyEvent): Boolean {
        if (event.key == Key.Enter) {
            executeCommand(inputText)
            return true
        }
        when (event.key) {
            Key.ArrowUp -> {
                if (historyIndex > 0) {
                    historyIndex--
                    inputText = history.getOrElse(historyIndex) { "" }
                }
                return true
            }
            Key.ArrowDown -> {
                if (historyIndex < history.size - 1) {
                    historyIndex++
                    inputText = history.getOrElse(historyIndex) { "" }
                } else {
                    historyIndex = history.size
                    inputText = ""
                }
                return true
            }
        }
        return false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // Output area
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black)
                .padding(8.dp)
        ) {
            items(outputLines) { line ->
                Text(
                    text = line.text,
                    color = if (line.isCommand) Color(0xFF00BFFF) else Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
            if (outputLines.isEmpty()) {
                item {
                    Text(
                        text = "Type a command and press Enter.",
                        color = Color.DarkGray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "% ",
                color = Color(0xFF00BFFF),
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 4.dp)
            )
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Enter command...", color = Color.DarkGray) },
                textStyle = TextStyle(
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(),
                modifier = Modifier
                    .weight(1f)
                    .onKeyEvent { handleKeyEvent(it) }
                    .background(Color(0xFF1A1A1A), shape = MaterialTheme.shapes.small)
                    .border(1.dp, Color(0xFF00BFFF).copy(alpha = 0.5f), shape = MaterialTheme.shapes.small),
                singleLine = true
            )
            IconButton(
                onClick = { executeCommand(inputText) },
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF00BFFF))
            }
        }
    }
}
