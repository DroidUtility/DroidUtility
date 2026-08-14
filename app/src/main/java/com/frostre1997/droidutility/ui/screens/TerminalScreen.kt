package com.frostre1997.droidutility.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import org.connectbot.term.TerminalView
import com.frostre1997.droidutility.managers.TerminalManager

@Composable
fun TerminalScreen() {
    val session = remember { TerminalManager.startSession() }

    DisposableEffect(Unit) {
        onDispose { /* keep session alive – or close if needed */ }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { context ->
                TerminalView(context, session).apply {
                    setTextColor(0xFF00FF00.toInt())   // green
                    setBackgroundColor(0xFF000000.toInt()) // black
                    setCursorColor(0xFFFFFFFF.toInt()) // white
                    setTextSize(16f)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
