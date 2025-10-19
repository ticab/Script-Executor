package org.example.scriptexecutor.output

import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextDecoration
import org.example.scriptexecutor.typography.MyColors


fun formatOutput(add: String, output: AnnotatedString, onClick: (line: Int, column: Int) -> Unit): AnnotatedString {
    val res = buildAnnotatedString {
        append(output)

        val compileError = Regex(""".*[/\\]\w+\.kts:(\d+):(\d+)""")

        for ((index, line) in add.lines().withIndex()) {
            val match = compileError.find(line)

            if (match != null) {
                val beforeText = line.take(match.range.first)
                append(beforeText)

                val linkText = "script:${match.groupValues[1]}:${match.groupValues[2]}"
                val startIndex = length

                withStyle(style = SpanStyle(color = MyColors.Blue, textDecoration = TextDecoration.Underline)) {
                    append(linkText)
                }
                val endIndex = length
                addLink(
                    LinkAnnotation.Clickable(
                        tag = "SCRIPT",
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = MyColors.Blue,
                                textDecoration = TextDecoration.Underline
                            )
                        ),
                        linkInteractionListener = {
                            val line = match.groupValues[1].toInt()
                            val column = match.groupValues[2].toInt()
                            onClick(line, column)
                        }
                    ),
                    start = startIndex,
                    end = endIndex
                )

                val afterText = line.substring(match.range.last + 1)
                append(afterText)
            }
            else{
                append(line)
            }
            if (index < add.lines().size - 1) append("\n")
        }

    }
    return res
}
