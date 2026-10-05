package com.frostre1997.droidutility.terminal

data class TerminalCell(
    val codepoint: Int,
    val fg: Int,
    val bg: Int,
    val flags: Int,
    val text: String
)

object CellFlags {
    const val BOLD = 1 shl 0
    const val ITALIC = 1 shl 1
    const val FAINT = 1 shl 2
    const val UNDERLINE = 1 shl 3
    const val STRIKETHROUGH = 1 shl 4
    const val INVERSE = 1 shl 5
    const val INVISIBLE = 1 shl 6
    const val WIDE = 1 shl 7
    const val SPACER = 1 shl 8
}
