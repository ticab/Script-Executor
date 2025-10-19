package org.example.scriptexecutor.output

import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextDecoration
import org.example.scriptexecutor.typography.MyColors


fun formatOutput(add: String, output: AnnotatedString, onClick: (line: Int, column: Int) -> Unit): AnnotatedString {
    val res = buildAnnotatedString {
        append(output)

        val compileErrorRegex = Regex(""".*[/\\]\w+\.kts:(\d+):(\d+)""")
        val runtimeErrorRegex = Regex("""at\s+Foo(\d+)\.<init>\(foo\d+.*\.kts:(\d+)\)""")

        for ((index, line) in add.lines().withIndex()) {
            val compileError = compileErrorRegex.find(line)
            val runtimeError = runtimeErrorRegex.find(line)

            if (compileError != null) {
                val beforeText = line.take(compileError.range.first)
                append(beforeText)

                val linkText = "script:${compileError.groupValues[1]}:${compileError.groupValues[2]}"
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
                            val line = compileError.groupValues[1].toInt()
                            val column = compileError.groupValues[2].toInt()
                            onClick(line, column)
                        }
                    ),
                    start = startIndex,
                    end = endIndex
                )

                val afterText = line.substring(compileError.range.last + 1)
                append(afterText)
            }
            else if (runtimeError != null) {
                append("at script.<init>(")
                var linkText = "script.kts:${runtimeError.groupValues[2]}"

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
                            val lineNum = runtimeError.groupValues[2].toInt()
                            onClick(lineNum, 1)
                        }
                    ),
                    start = startIndex,
                    end = endIndex
                )
                append(")")
            }

            else{
                append(line)
            }
            if (index < add.lines().size - 1) append("\n")
        }

    }
    return res
}
