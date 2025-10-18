package org.example.scriptexecutor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import org.example.scriptexecutor.typography.MyTypography
import java.io.File
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.channels.Channel
import org.example.scriptexecutor.typography.MyColors
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun App() {
    var code by remember { mutableStateOf(TextFieldValue("")) }
    var output by remember { mutableStateOf(AnnotatedString("")) }
    var isRunning by remember { mutableStateOf(false) }
    var isStopping by remember { mutableStateOf(false) }
    var exitCode by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()
    var runJob by remember { mutableStateOf<Job?>(null) }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Kotlin Script Executor",
                        style = MyTypography.Title
                    )
                    Text(
                        text = "Enter script, execute it, and see its output side-by-side",
                        style = MyTypography.ThinGrey
                    )
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        runJob = scope.launch {
                            isRunning = true
                            output = AnnotatedString("")
                            exitCode = null
                            val codeExit = runScript(code.text) { line -> output = buildOutput(line, output) {
                                line, column -> println("Clicked script at line $line, col $column")
                            }}
                            exitCode = codeExit
                            isRunning = false
                        }
                    },
                    enabled = !isRunning,
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MyColors.Green),
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Text("Run", style = MyTypography.Body)
                }
                Button(
                    onClick = {
                        scope.launch {
                            isRunning = false
                            isStopping = true
                            val stopCode = stopScript()
                            exitCode = stopCode

                            runJob?.cancelAndJoin()

                            output = buildAnnotatedString {
                                append(output)
                                append(annotatedOutput("Script stopped (code ${stopCode})\n", Color.Red ))
                            }
                            isStopping = false
                        }
                    },
                    enabled = isRunning && !isStopping,
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MyColors.Red)
                ) {
                    Text("Stop", style = MyTypography.Body)
                }

            }

            HorizontalDivider(
                color = Color.Gray,
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp)
            ) {
                Text(
                    text = "Code Editor",
                    style = MyTypography.TitleSmall,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Output",
                        style = MyTypography.TitleSmall
                    )

                    Spacer(Modifier.weight(1f))

                    Text(
                        text =  when {
                            isRunning -> "Running"
                            isStopping -> "Stopping"
                            exitCode == null -> "Not started"
                            exitCode == 0 -> "Success"
                            else -> "Exit code: $exitCode"
                        },
                        style = MyTypography.BodySmall
                    )
                }
            }
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                CodeEditor(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    value = code,
                    onValueChange = {
                        code = it;
                    }
                )
                CodeOutput(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    output = output
                )
            }
        }
    }
}

fun buildOutput(add: String, output: AnnotatedString, onClick: (line: Int, column: Int) -> Unit): AnnotatedString{
    val res = buildAnnotatedString {
        append(output)

        //Regex("""at\s+Foo\d+\.<init>\(foo\d+.*\.kts:(\d+)\)"""),
        val regex = Regex(""".*[/\\]\w+\.kts:(\d+):(\d+)""")

        for (line in add.lines()) {
            val match = regex.find(line)

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
            } else {
                append(line)
            }
            append("\n")
        }
    }
    return res
}
