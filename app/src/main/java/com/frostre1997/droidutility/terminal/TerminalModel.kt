package com.frostre1997.droidutility.terminal

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Cell(
    val char: Char = ' ',
    val foreground: Int = 0x00FF00, // green
    val background: Int = 0x000000  // black
)

class TerminalModel(
    val columns: Int = 80,
    val rows: Int = 24
) {
    private val _grid = MutableStateFlow(List(rows) { Array(columns) { Cell() } })
    val grid: StateFlow<List<Array<Cell>>> = _grid.asStateFlow()

    private var cursorRow = 0
    private var cursorCol = 0
    private var currentFg = 0x00FF00
    private var currentBg = 0x000000

    // Channel for sending user input to the shell process
    val inputChannel = Channel<String>()

    fun writeChar(c: Char) {
        when (c) {
            '\r' -> cursorCol = 0
            '\n' -> {
                cursorRow++
                if (cursorRow >= rows) scrollUp()
            }
            '\b' -> {
                if (cursorCol > 0) cursorCol--
            }
            '\u001B' -> {
                // ANSI escape code – we'll parse in a separate function
                // For simplicity, we'll handle it in the reading loop
            }
            else -> {
                if (cursorCol < columns) {
                    _grid.value[cursorRow][cursorCol] = Cell(c, currentFg, currentBg)
                    cursorCol++
                } else {
                    // wrap to next line
                    cursorRow++
                    cursorCol = 0
                    if (cursorRow >= rows) scrollUp()
                }
            }
        }
        updateCursor()
    }

    private fun scrollUp() {
        val newGrid = _grid.value.toMutableList()
        newGrid.removeAt(0)
        newGrid.add(Array(columns) { Cell() })
        _grid.value = newGrid
        cursorRow = rows - 1
    }

    private fun updateCursor() {
        // Store cursor position in a separate state if needed
        // For now, we just update the grid and let the view draw a cursor
    }

    fun parseAnsi(seq: String) {
        // Very basic: handle clear and cursor home
        when {
            seq == "[2J" -> clearScreen()
            seq == "[H" -> homeCursor()
            seq.startsWith("[") && seq.endsWith("H") -> {
                val parts = seq.removePrefix("[").removeSuffix("H").split(";")
                if (parts.size == 2) {
                    val row = parts[0].toIntOrNull()?.minus(1) ?: 0
                    val col = parts[1].toIntOrNull()?.minus(1) ?: 0
                    cursorRow = row.coerceIn(0, rows - 1)
                    cursorCol = col.coerceIn(0, columns - 1)
                }
            }
            // Colours: [31m, [32m, etc.
            seq.startsWith("[") && seq.endsWith("m") -> {
                val code = seq.removePrefix("[").removeSuffix("m").toIntOrNull()
                when (code) {
                    31 -> currentFg = 0xFF0000 // red
                    32 -> currentFg = 0x00FF00 // green
                    33 -> currentFg = 0xFFFF00 // yellow
                    34 -> currentFg = 0x0000FF // blue
                    35 -> currentFg = 0xFF00FF // magenta
                    36 -> currentFg = 0x00FFFF // cyan
                    37 -> currentFg = 0xFFFFFF // white
                    0  -> currentFg = 0x00FF00 // reset to default
                }
            }
        }
    }

    fun clearScreen() {
        _grid.value = List(rows) { Array(columns) { Cell() } }
        cursorRow = 0
        cursorCol = 0
    }

    fun homeCursor() {
        cursorRow = 0
        cursorCol = 0
    }

    fun writeString(text: String) {
        var i = 0
        while (i < text.length) {
            if (text[i] == '\u001B') {
                // ANSI sequence starts with ESC
                val start = i
                i++ // skip ESC
                val seq = StringBuilder()
                while (i < text.length && text[i] != 'm' && text[i] != 'H' && text[i] != 'J') {
                    seq.append(text[i])
                    i++
                }
                if (i < text.length) {
                    seq.append(text[i]) // include the terminating char
                    parseAnsi(seq.toString())
                    i++
                }
            } else {
                writeChar(text[i])
                i++
            }
        }
    }
}
