package com.frostre1997.droidutility.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asAndroidPaint
import com.frostre1997.droidutility.terminal.TerminalModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun TerminalScreen() {
    val model = remember { TerminalModel(columns = 80, rows = 24) }
    var shellProcess by remember { mutableStateOf<Process?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Start the shell process
    LaunchedEffect(Unit) {
        // Use ShellManager to start an interactive shell
        val process = try {
            val hasSu = java.io.File("/system/bin/su").exists()
            if (hasSu) {
                Runtime.getRuntime().exec(arrayOf("su", "-c", "sh"))
            } else {
                Runtime.getRuntime().exec(arrayOf("/system/bin/sh"))
            }
        } catch (_: Exception) {
            Runtime.getRuntime().exec(arrayOf("/system/bin/sh"))
        }
        shellProcess = process

        // Set the prompt via PS1 (cosmetic)
        process.outputStream.use {
            it.write("export PS1='$ '\n".toByteArray())
            it.flush()
        }

        // Read stdout in background
        coroutineScope.launch(Dispatchers.IO) {
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val buffer = CharArray(1024)
            var len: Int
            while (process.isAlive && reader.read(buffer).also { len = it } != -1) {
                val chunk = String(buffer, 0, len)
                withContext(Dispatchers.Main) {
                    model.writeString(chunk)
                }
            }
        }

        // Send user input from the channel to stdin
        coroutineScope.launch {
            for (input in model.inputChannel) {
                process.outputStream.write(input.toByteArray())
                process.outputStream.flush()
            }
        }
    }

    val grid by model.grid.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.Enter -> {
                            coroutineScope.launch { model.inputChannel.send("\n") }
                            true
                        }
                        Key.Backspace -> {
                            coroutineScope.launch { model.inputChannel.send("\b") }
                            true
                        }
                        Key.ArrowUp -> {
                            coroutineScope.launch { model.inputChannel.send("\u001B[A") }
                            true
                        }
                        Key.ArrowDown -> {
                            coroutineScope.launch { model.inputChannel.send("\u001B[B") }
                            true
                        }
                        Key.ArrowRight -> {
                            coroutineScope.launch { model.inputChannel.send("\u001B[C") }
                            true
                        }
                        Key.ArrowLeft -> {
                            coroutineScope.launch { model.inputChannel.send("\u001B[D") }
                            true
                        }
                        else -> {
                            val c = event.key.code.toChar()
                            if (c.isLetterOrDigit() || c == ' ' || c in "!@#$%^&*()-_=+[]{};:'\",.<>/?") {
                                coroutineScope.launch { model.inputChannel.send(c.toString()) }
                                true
                            } else false
                        }
                    }
                } else false
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val fontSize = with(density) { 16.sp.toPx() }
            val charWidth = fontSize * 0.6f // approximate monospace width
            val charHeight = fontSize * 1.2f

            val paint = Paint().apply {
                this.textSize = fontSize
                typeface = FontFamily.Monospace.toTypeface()
            }

            for (row in grid.indices) {
                for (col in grid[row].indices) {
                    val cell = grid[row][col]
                    val x = col * charWidth
                    val y = (row + 1) * charHeight

                    // Draw background
                    drawRect(
                        color = Color(cell.background),
                        topLeft = androidx.compose.ui.geometry.Offset(x, y - charHeight),
                        size = androidx.compose.ui.geometry.Size(charWidth, charHeight)
                    )

                    // Draw character
                    drawContext.canvas.nativeCanvas.drawText(
                        cell.char.toString(),
                        x,
                        y - 2f,
                        paint.asAndroidPaint().apply { color = cell.foreground }
                    )
                }
            }

            // Draw cursor (blinking rectangle)
            val cursorRow = model.cursorRow
            val cursorCol = model.cursorCol
            drawRect(
                color = Color.White,
                topLeft = androidx.compose.ui.geometry.Offset(cursorCol * charWidth, cursorRow * charHeight),
                size = androidx.compose.ui.geometry.Size(charWidth, charHeight),
                alpha = 0.5f
            )
        }
    }
}
