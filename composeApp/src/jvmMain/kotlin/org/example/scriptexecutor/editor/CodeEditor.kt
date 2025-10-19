package org.example.scriptexecutor.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.sp
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

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    LaunchedEffect(value) {
        state.updateCursor(value)
        focusRequester.requestFocus()
    }

    Column(modifier = modifier) {
        Column(
            modifier = modifier
                .background(color = MyColors.DarkGrey, shape = RoundedCornerShape(5.dp))
                .verticalScroll(scrollState)
                .weight(0.9f)
                .padding(top = 4.dp)
                .drawBehind { drawCurrentLineHighlight(state) }
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    focusRequester.requestFocus()
                }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.width(40.dp).padding(start = 4.dp)
                ) {
                    val lines = value.toString().lines()
                    for (i in lines.indices) {
                        Text(
                            text = "${i + 1}",
                            style = MyTypography.Code
                        )
                    }
                }

                VerticalDivider(
                    color = Color.Gray,
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxHeight()
                )

                BasicTextField(
                    value = value,
                    onValueChange = { newCode ->
                        state.updateCursor(newCode)
                        onValueChange(newCode)
                    },
                    textStyle = MyTypography.Code,
                    cursorBrush = SolidColor(Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 5.dp)
                        .focusRequester(focusRequester)
                        .onFocusChanged { state.isFocused = it.isFocused }
                        .onPreviewKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown && event.key == Key.Tab) {
                                val cursor = value.selection.start
                                val newText = StringBuilder(value.text).apply {
                                    insert(cursor, "    ") // Insert 4 spaces
                                }.toString()

                                val newValue = value.copy(
                                    text = newText,
                                    selection = TextRange(cursor + 4)
                                )
                                onValueChange(newValue)
                                true
                            } else {
                                false
                            }
                        },
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
            Spacer(Modifier.weight(1f))

            Text(
                text = "${state.line}:${state.column}",
                style = MyTypography.BodySmall
            )
        }
    }
}

private fun DrawScope.drawCurrentLineHighlight(state: CodeEditorState) {
    val lineHeight = 19.sp.toPx()
    if (state.isFocused) {
        drawRect(
            color = MyColors.LightGrey.copy(alpha = 0.2f),
            topLeft = Offset(0f, (state.line - 1) * lineHeight),
            size = Size(size.width, lineHeight)
        )
    }
}