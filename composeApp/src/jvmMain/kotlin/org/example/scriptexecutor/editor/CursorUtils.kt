package org.example.scriptexecutor.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

fun getCursorLineAndColumn(value: TextFieldValue, setCursor: (Pair<Int, Int>) -> Unit) {
    val cursor = value.selection.start
    val text = value.text

    var line = 1
    var column = 1

    for (i in 0 until cursor) {
        if (text[i] == '\n') {
            line++
            column = 1
        } else {
            column++
        }
    }
    setCursor(line to column)
}

fun TextFieldValue.moveCursor(line: Int, column: Int): TextFieldValue {
    val lines = text.lines()
    var offset = lines.take(line - 1).sumOf { it.length + 1 } // +1 for newline
    offset += column - 1
    offset = offset.coerceIn(0, text.length)
    return copy(selection = TextRange(offset))
}