package com.frostre1997.droidutility.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.android.terminal_emulator.TerminalView
import com.frostre1997.droidutility.core.TerminalManager

@Composable
fun TerminalScreen() {
    val session = remember { TerminalManager.startSession() }

    DisposableEffect(Unit) {
        onDispose { }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { context ->
                TerminalView(context).apply {
                    attachSession(session)
                    setUseWideCharacterFormatting(true)
                    setTextColor(0xFF00FF00.toInt())
                    setBackgroundColor(0xFF000000.toInt())
                    setCursorColor(0xFFFFFFFF.toInt())
                    setTextSize(16f)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
