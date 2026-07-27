package com.frostre1997.droidutility.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frostre1997.droidutility.ShizukuShellManager
import kotlinx.coroutines.launch

fun Context.toast(message: String) {
    android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
}

data class TerminalLine(val text: String, val isCommand: Boolean)

@Composable
fun TerminalScreen() {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    val outputLines = remember { mutableStateListOf<TerminalLine>() }
    val scrollState = rememberLazyListState()
    var history by remember { mutableStateOf(listOf<String>()) }
    var historyIndex by remember { mutableStateOf(-1) }

    fun scrollToBottom() {
        coroutineScope.launch {
            if (outputLines.isNotEmpty()) {
                scrollState.animateScrollToItem(outputLines.size - 1)
            }
        }
    }

    fun executeCommand(command: String) {
        if (command.isBlank()) return
        outputLines.add(TerminalLine("> $command", isCommand = true))
        history = history + command
        historyIndex = history.size

        coroutineScope.launch {
            val result = ShizukuShellManager.executeCommand(command)
            if (result.success) {
                val output = result.output
                if (output.isNotBlank()) {
                    outputLines.addAll(output.lines().map { TerminalLine(it, isCommand = false) })
                } else {
                    outputLines.add(TerminalLine("(no output)", isCommand = false))
                }
            } else {
                val error = result.error.ifBlank { "Command failed (exit code: ${result.exitCode})" }
                outputLines.addAll(error.lines().map { TerminalLine("ERROR: $it", isCommand = false) })
            }
            if (result.error.isNotBlank() && result.success) {
                outputLines.addAll(result.error.lines().map { TerminalLine("STDERR: $it", isCommand = false) })
            }
            scrollToBottom()
        }
        inputText = ""
        scrollToBottom()
    }

    fun clearOutput() {
        outputLines.clear()
    }

    fun copyAllOutput() {
        val text = outputLines.joinToString("
") { it.text }
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Terminal Output", text)
        cm.setPrimaryClip(clip)
        context.toast("Copied to clipboard")
    }

    fun handleKeyEvent(event: KeyEvent): Boolean {
        if (event.key == Key.Enter) {
            executeCommand(inputText)
            return true
        }
        return false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Terminal",
                color = Color(0xFF00FF00),
                fontSize = 18.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row {
                IconButton(
                    onClick = { copyAllOutput() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF00FF00))
                }
                IconButton(
                    onClick = { clearOutput() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF00FF00))
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF0A0A0A), RoundedCornerShape(8.dp))
                .clickable { /* ignore */ }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                state = scrollState
            ) {
                items(outputLines) { line ->
                    Text(
                        text = line.text,
                        color = if (line.isCommand) Color(0xFF00FF00) else Color(0xFFF0F0F0),
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
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "> ",
                color = Color(0xFF00FF00),
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 4.dp)
            )
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Enter command...", color = Color.DarkGray) },
                textStyle = TextStyle(
                    color = Color(0xFFF0F0F0),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(),
                modifier = Modifier
                    .weight(1f)
                    .onKeyEvent { handleKeyEvent(it) }
                    .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF00FF00).copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                singleLine = true
            )
            IconButton(
                onClick = { executeCommand(inputText) },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF00FF00))
            }
        }
    }
                                            }
