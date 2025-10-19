package org.example.scriptexecutor.editor

import androidx.compose.runtime.*
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun rememberCodeEditorState(initialValue: TextFieldValue): CodeEditorState {
    return remember { CodeEditorState(initialValue) }
}

class CodeEditorState(initialValue: TextFieldValue) {
    var line by mutableStateOf(1)
    var column by mutableStateOf(1)
    var isFocused by mutableStateOf(false)

    fun updateCursor(value: TextFieldValue) {
        getCursorLineAndColumn(value) { (l, c) ->
            line = l
            column = c
        }
    }
}