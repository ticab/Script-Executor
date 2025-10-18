package org.example.scriptexecutor

import androidx.compose.runtime.remember
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.*
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.scriptexecutor.typography.MyColors
import org.example.scriptexecutor.typography.MyTypography

private val keywords = setOf(
    "fun", "val", "var", "class", "object", "interface", "package", "import",
    "if", "else", "for", "while", "when", "return", "break", "continue", "throw", "try", "catch", "finally",
    "true", "false", "null",
    "abstract", "open", "override", "private", "protected", "public", "internal", "companion", "data", "enum", "sealed", "inline", "const", "lateinit", "suspend"
)

@Composable
fun CodeEditor(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    var cursorPos by remember { mutableStateOf(1 to 1) }
    val scrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    LaunchedEffect(value) {
        getCursorLineAndColumn(value) { cursorPos = it }
        focusRequester.requestFocus()
    }

    Column(modifier = modifier) {
        Column(
            modifier = modifier
                .background(color = MyColors.DarkGrey, shape = RoundedCornerShape(5.dp))
                .verticalScroll(scrollState)
                .weight(0.9f)
                .padding(top = 4.dp)
                .drawBehind {
                    val lineHeight = 19.sp.toPx()
                    val lines = value.text.split("\n")
                    val highlightLine = cursorPos.first - 1

                    lines.forEachIndexed { i, _ ->
                        if (i == highlightLine && isFocused) {
                            drawRect(
                                color = MyColors.LightGrey.copy(alpha = 0.2f),
                                topLeft = Offset(0f, i * lineHeight),
                                size = Size(size.width, lineHeight)
                            )
                        }
                    }
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
                        getCursorLineAndColumn(newCode, setCursor = { cursorPos = it })
                        onValueChange(newCode)
                    },
                    textStyle = MyTypography.Code,
                    cursorBrush = SolidColor(Color.White),
                    modifier = Modifier.weight(1f).padding(start = 5.dp).focusRequester(focusRequester)
                        .onFocusChanged { isFocused = it.isFocused },
                    visualTransformation = {
                        TransformedText(highlightCode(it.text), OffsetMapping.Identity)
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
                text = "${cursorPos.first}:${cursorPos.second}",
                style = MyTypography.BodySmall
            )
        }
    }
}

private fun highlightCode(text: String): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val tokens = Regex("""("[^"]*"|"[^"\n]*$|\d+|\w+|[^\w\s]|[\s]+)""")
        .findAll(text)

    for (token in tokens) {
        val part = token.value

        when {
            // Strings
            part.startsWith("\"") && part.endsWith("\"") ->
                builder.withStyle(SpanStyle(color = MyColors.Green)) { append(part) }

            // Keywords
            part in keywords ->
                builder.withStyle(SpanStyle(color = Color(0xFFCC7832))) { append(part) }

            // Numbers
            part.matches(Regex("\\d+")) ->
                builder.withStyle(SpanStyle(color = Color(0xFF6897BB))) { append(part) }

            // Default
            else -> builder.append(part)
        }
    }

    return builder.toAnnotatedString()
}

private fun getCursorLineAndColumn(value: TextFieldValue, setCursor: (Pair<Int, Int>) -> Unit){
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

fun TextFieldValue.withCursorAt(line: Int, column: Int): TextFieldValue {
    val lines = this.text.lines()
    var offset = lines.take(line - 1).sumOf { it.length + 1 } // +1 for newline
    offset += column - 1
    offset = offset.coerceIn(0, this.text.length)
    return this.copy(selection = TextRange(offset))
}
