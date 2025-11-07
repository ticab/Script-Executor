package org.example.scriptexecutor.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

class CursorCache(private var text: String) {
    private var lineStartOffsets: IntArray = getLineStarts(text)

    fun updateText(newText: String) {
        if (newText !== text) {
            text = newText
            lineStartOffsets = getLineStarts(newText)
        }
    }

    private fun getLineStarts(text: String): IntArray {
        val starts = mutableListOf(0)
        for (i in text.indices) if (text[i] == '\n') starts += (i + 1)
        return starts.toIntArray()
    }

    fun getLineAndColumn(cursor: Int, setCursor: (Pair<Int, Int>) -> Unit) {
        var low = 0
        var high = lineStartOffsets.size - 1
        var index = 0

        while (low <= high) {
            val mid = low + (high - low) / 2
            if (lineStartOffsets[mid] <= cursor) {
                index = mid
                low = mid + 1
            }
            else {
                high = mid - 1
            }
        }
        val column = cursor - lineStartOffsets[index] + 1
        setCursor(index + 1 to column)
    }
}

fun TextFieldValue.moveCursor(line: Int, column: Int): TextFieldValue {
    val lines = text.lines()
    var offset = lines.take(line - 1).sumOf { it.length + 1 } // +1 for newline
    offset += column - 1
    offset = offset.coerceIn(0, text.length)
    return copy(selection = TextRange(offset))
}

fun findBraceAtCursor(code: String, cursor: Int): Int? {
    if (cursor > 0 && code[cursor - 1] in "{}()[]") return cursor - 1
    if (cursor < code.length && code[cursor] in "{}()[]") return cursor
    return null
}

fun findMatchingBrace(code: String, index: Int): Int? {
    val brace = code[index]

    val pairs = mapOf('(' to ')', '[' to ']', '{' to '}')
    val reversedPairs = pairs.entries.associate { (k, v) -> v to k }

    val isOpenBrace = brace in pairs.keys
    val direction = if(isOpenBrace) 1 else -1
    val match = if(isOpenBrace) pairs[brace] else reversedPairs[brace]

    var depth = 1
    var i = index + direction

    while (i in code.indices) {
        when (code[i]) {
            brace -> depth++
            match -> depth--
        }
        if (depth == 0) return i
        i += direction
    }
    return null
}