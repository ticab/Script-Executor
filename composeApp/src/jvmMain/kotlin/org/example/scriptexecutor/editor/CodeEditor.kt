package org.example.scriptexecutor.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import com.facebook.ktfmt.format.Formatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.scriptexecutor.typography.MyColors
import org.example.scriptexecutor.typography.MyTypography

@Composable
fun CodeEditor(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()
    val state = rememberCodeEditorState(value)
    val coroutineScope = rememberCoroutineScope()
    val tabSpace = "    "

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    LaunchedEffect(value) {
        state.updateCursor(value)
        focusRequester.requestFocus()
    }
    LaunchedEffect(value.text) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(modifier = modifier) {
        Text(
            text = "Code Editor",
            style = MyTypography.TitleSmall,
            modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp)
        )
        Column(
            modifier = modifier
                .background(color = MyColors.DarkGrey, shape = RoundedCornerShape(5.dp))
                .verticalScroll(scrollState)
                .padding(top = 4.dp)
                .drawBehind {
                    drawCurrentLineHighlight(state)
                    drawBraceGuides(value.text, state)
                }
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { focusRequester.requestFocus() }
                .weight(1.0f)
        ) {
            Row( modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.width(40.dp).padding(start = 4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    val lines = value.toString().lines()
                    for (i in lines.indices) {
                        Text(
                            text = "${i + 1}",
                            style = MyTypography.Code
                        )
                    }
                }

                BasicTextField(
                    value = value,
                    onValueChange = { newCode ->
                        state.updateCursor(newCode)
                        onValueChange(newCode)
                    },
                    textStyle = MyTypography.Code,
                    cursorBrush = SolidColor(Color.White),
                    onTextLayout = { state.textLayoutResult = it},
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 5.dp)
                        .focusRequester(focusRequester)
                        .onFocusChanged { state.isFocused = it.isFocused }
                        .onPreviewKeyEvent { event -> handleKeyEvent(event, value, onValueChange, tabSpace) },
                    visualTransformation = {
                        TransformedText(
                            CodeHighlighter.highlightCode(it.text),
                            OffsetMapping.Identity
                        )
                    }
                )
            }
        }

        Row(
            modifier = Modifier.weight(0.1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    state.isFormatting = true
                    CoroutineScope(Dispatchers.Default).launch {
                        val newCode = Formatter.format(value.text)
                        withContext(Dispatchers.Main) {
                            onValueChange(value.copy(text = newCode))
                            state.isFormatting = false
                        }
                    }
                },
                enabled = value.text.isNotEmpty() && !state.isFormatting,
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MyColors.Blue)
            ) {
                Text("Fomat Code", style = MyTypography.Body)
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = "Line: ${state.line}, Column: ${state.column}",
                style = MyTypography.BodySmall
            )
        }
    }
}

private fun handleKeyEvent(event: KeyEvent, value: TextFieldValue, onValueChange: (TextFieldValue) -> Unit, tabSpace: String): Boolean {
    if (event.type != KeyEventType.KeyDown) return false

    val cursor = value.selection.start
    val text = value.text

    when (event.key) {
        Key.Tab -> {
            val newText = StringBuilder(text).apply {
                insert(cursor, tabSpace)
            }.toString()
            val newValue = value.copy(
                text = newText,
                selection = TextRange(cursor + 4)
            )
            onValueChange(newValue)
            return true
        }

        Key.Enter -> {
            val textBeforeCursor = value.text.substring(0, cursor)
            val lastLineBreak = textBeforeCursor.lastIndexOf('\n')
            val textAfterCursor = text.substring(cursor)
            val currentLineStart = if (lastLineBreak == -1) 0 else lastLineBreak + 1
            val currentLine = textBeforeCursor.substring(currentLineStart)

            val leadingWhitespace = currentLine.takeWhile { it == ' ' || it == '\t' }

            val prevChar = textBeforeCursor.lastOrNull()

            if (prevChar == '{') {
                val newText = buildString {
                    append(textBeforeCursor)
                    append("\n$leadingWhitespace$tabSpace")
                    append("\n$leadingWhitespace}")
                    append(textAfterCursor)
                }
                val newCursor = cursor + 1 + leadingWhitespace.length + 4
                val newValue = value.copy(
                    text = newText,
                    selection = TextRange(newCursor)
                )
                onValueChange(newValue)
                return true
            } else {
                val newText = buildString {
                    append(textBeforeCursor)
                    append("\n$leadingWhitespace")
                    append(textAfterCursor)
                }
                val newValue = value.copy(
                    text = newText,
                    selection = TextRange(cursor + 1 + leadingWhitespace.length)
                )
                onValueChange(newValue)
                return true
            }
        }

        Key.Backspace -> {
            if (cursor >= 4) {
                val before = text.substring(cursor - 4, cursor)
                if (before == tabSpace) {
                    val newText = buildString {
                        append(text.substring(0, cursor - 4))
                        append(text.substring(cursor))
                    }
                    val newValue = value.copy(
                        text = newText,
                        selection = TextRange(cursor - 4)
                    )
                    onValueChange(newValue)
                    return true
                }
            }
        }
    }
    return false
}

private fun DrawScope.drawCurrentLineHighlight(state: CodeEditorState) {
    if (state.isFocused) {
        state.textLayoutResult?.let { layout ->
            val line = state.line - 1
            if (line in 0 until layout.lineCount) {
                val top = layout.getLineTop(line)
                val bottom = layout.getLineBottom(line)
                drawRect(
                    color = MyColors.LightGrey.copy(alpha = 0.2f),
                    topLeft = Offset(0f, top),
                    size = Size(size.width, bottom - top)
                )
            }
        }
    }
}

private fun DrawScope.drawBraceGuides(code: String, state: CodeEditorState) {
    val layout = state.textLayoutResult ?: return

    val lineNumberColumnWidth = 40.dp.toPx()
    val paddingStart = 5.dp.toPx()
    val charWidth = 11f
    val tabSize = 4
    var level = 0

    code.lines().forEachIndexed { i, line ->
        val top = layout.getLineTop(i)
        val bottom = layout.getLineBottom(i)

        val closingBraces = line.count { it == '}' }
        repeat(closingBraces.coerceAtMost(level)) { level-- }


        repeat(level) { l ->
            val x = lineNumberColumnWidth + paddingStart + l * charWidth * tabSize
            drawLine(
                color = Color.Gray.copy(alpha = 0.4f),
                start = Offset(x, top),
                end = Offset(x, bottom),
                strokeWidth = 1.2f
            )
        }

        val openingBraces = line.count { it == '{' }
        repeat(openingBraces) { level++ }
    }
}

