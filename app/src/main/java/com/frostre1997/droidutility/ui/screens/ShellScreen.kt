package com.frostre1997.droidutility.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frostre1997.droidutility.core.ShellManager
import kotlinx.coroutines.launch

@Composable
fun ShellScreen() {
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    val outputLines = remember { mutableStateListOf<String>() }
    var history by remember { mutableStateOf(listOf<String>()) }
    var historyIndex by remember { mutableStateOf(-1) }

    fun executeCommand(command: String) {
        if (command.isBlank()) return
        outputLines.add("% $command")
        history = history + command
        historyIndex = history.size

        coroutineScope.launch {
            val result = ShellManager.executeCommand(command)
            if (result.success) {
                outputLines.add(result.output.ifBlank { "(no output)" })
            } else {
                outputLines.add("Error: ${result.error}")
            }
        }
        inputText = ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // Output
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black)
                .padding(8.dp)
        ) {
            items(outputLines) { line ->
                Text(
                    text = line,
                    color = if (line.startsWith("%")) Color(0xFF00BFFF) else Color.White,
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

        // Input row with plain % prompt
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
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color.White,
                    containerColor = Color.Transparent
                ),
                modifier = Modifier
                    .weight(1f)
                    .onKeyEvent { event ->
                        if (event.key == Key.Enter) {
                            executeCommand(inputText)
                            true
                        } else false
                    },
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
