package org.example.scriptexecutor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


@Volatile
private var currentProcess: Process? = null

suspend fun runScript(code: String, onOutput: (String) -> Unit): Int = withContext(Dispatchers.IO) {
    val script = File.createTempFile("foo", ".kts")
    script.writeText(code)

    val processBuilder = ProcessBuilder("kotlinc", "-script", script.absolutePath)
    processBuilder.redirectErrorStream(true)
    val process = processBuilder.start()

    val reader = BufferedReader(InputStreamReader(process.inputStream, Charsets.UTF_8))

    val outputChannel = Channel<String>(capacity = Channel.UNLIMITED)

    val readerJob = launch {
        val buffer = StringBuilder()
        val charBuf = CharArray(512)

        while (true) {
            val readCount = reader.read(charBuf)
            if (readCount == -1) break

            for (i in 0 until readCount) {
                val ch = charBuf[i]
                buffer.append(ch)
                if (ch == '\n') {
                    outputChannel.trySend(buffer.toString())
                    buffer.clear()
                }
            }

            if (buffer.isNotEmpty()) {
                outputChannel.trySend(buffer.toString())
                buffer.clear()
            }
        }
        if (buffer.isNotEmpty()) {
            outputChannel.trySend(buffer.toString())
        }
        outputChannel.close()
    }

    val uiJob = launch(Dispatchers.Main) {
        for (chunk in outputChannel) {
            onOutput(chunk)
        }
    }
    val exitCode = try {
        process.waitFor()
    } catch (e: InterruptedException) {
        130
    } finally {
        reader.close()
        outputChannel.close()

        readerJob.cancel()
        uiJob.cancel()
        currentProcess = null
    }
    exitCode
}

suspend fun stopScript(): Int = withContext(Dispatchers.IO) {
    currentProcess?.let { proc ->
        if (proc.isAlive) {
            proc.destroy()
            delay(100)
            if (proc.isAlive) proc.destroyForcibly()
            return@withContext 130
        }
    }
    return@withContext -1
}

fun annotatedOutput(output: String, color: Color): AnnotatedString {
    return buildAnnotatedString {
        withStyle(style = SpanStyle(color = color)) {
            append(output)
        }
    }
}
