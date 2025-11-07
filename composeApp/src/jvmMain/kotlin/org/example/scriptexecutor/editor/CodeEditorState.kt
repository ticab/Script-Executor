package org.example.scriptexecutor.editor

import androidx.compose.runtime.*
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun rememberCodeEditorState(initialValue: TextFieldValue): CodeEditorState {
    return remember { CodeEditorState(initialValue) }
}

class CodeEditorState(initialValue: TextFieldValue) {
    var line by mutableStateOf(1)
    var column by mutableStateOf(1)
    var isFocused by mutableStateOf(false)
    var textLayoutResult by mutableStateOf<TextLayoutResult?>(null)
    var isFormatting by mutableStateOf(false)

    private val cursorCache = CursorCache(initialValue.text)

    fun updateCursor(value: TextFieldValue) {
        cursorCache.updateText(value.text)
        cursorCache.getLineAndColumn(value.selection.start) { (l, c) ->
            line = l
            column = c
        }
    }
}