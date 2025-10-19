package org.example.scriptexecutor.editor

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import org.example.scriptexecutor.typography.MyColors
import androidx.compose.ui.graphics.Color

object CodeHighlighter {
    private val keywords = setOf(
        "fun", "val", "var", "class", "object", "interface", "package", "import",
        "if", "else", "for", "while", "when", "return", "break", "continue", "throw", "try", "catch", "finally",
        "true", "false", "null",
        "abstract", "open", "override", "private", "protected", "public", "internal", "companion", "data", "enum", "sealed", "inline", "const", "lateinit", "suspend"
    )

    fun highlightCode(text: String): AnnotatedString {
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
}