package org.example.scriptexecutor

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import org.example.scriptexecutor.typography.MyTypography
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import org.example.scriptexecutor.editor.CodeEditor
import org.example.scriptexecutor.editor.moveCursor
import org.example.scriptexecutor.output.CodeOutput
import org.example.scriptexecutor.output.formatOutput
import org.example.scriptexecutor.typography.MyColors
import java.awt.Cursor

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
                            val codeExit = runScript(code.text) { line ->
                                output = formatOutput(line, output) { line, column ->
                                    code = code.moveCursor(line, column)
                                }
                            }
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


            BoxWithConstraints(Modifier.fillMaxSize()) {
                val totalWidth = maxWidth
                val density = LocalDensity.current

                var editorWidth by remember { mutableStateOf(totalWidth / 2) }

                LaunchedEffect(totalWidth) {
                    editorWidth = totalWidth / 2
                }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    CodeEditor(
                        modifier = Modifier.width(editorWidth).fillMaxHeight(),
                        value = code,
                        onValueChange = {
                            code = it
                        }
                    )

                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .cursorForResize()
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures { _, dragAmount ->
                                    val dragDp = with(density) { dragAmount.toDp() }
                                    editorWidth = (editorWidth + dragDp)
                                        .coerceIn(300.dp, totalWidth - 300.dp)
                                }
                            }
                    )

                    CodeOutput(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        output = output,
                        cleanOutput = {
                            output = buildAnnotatedString { }
                        },
                        stateText = when {
                            isRunning -> "Running"
                            isStopping -> "Stopping"
                            exitCode == null -> "Not started"
                            exitCode == 0 -> "Success"
                            else -> "Exit code: $exitCode"
                        }
                    )
                }
            }
        }
    }
}
fun Modifier.cursorForResize(): Modifier = this.pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
