package org.example.scriptexecutor.output

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import org.example.scriptexecutor.typography.MyColors
import org.example.scriptexecutor.typography.MyTypography
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
fun CodeOutput(
    output: AnnotatedString,
    modifier: Modifier = Modifier,
    cleanOutput: () -> Unit,
    stateText: String
) {
    val scrollState = rememberScrollState()

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp)
        ) {
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
                    text = stateText,
                    style = MyTypography.BodySmall
                )
            }
        }
        Column(
            modifier = modifier
                .background(color = MyColors.DarkGrey, shape = RoundedCornerShape(5.dp))
                .verticalScroll(scrollState)
                .padding(4.dp).weight(1.0f)
        ) {
            BasicText(modifier = Modifier.fillMaxWidth(), text = output, style = MyTypography.Code)
        }
        Row(
            modifier = Modifier.weight(0.1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(Modifier.weight(1f))

            Button(
                onClick = cleanOutput,
                enabled = output.isNotEmpty(),
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MyColors.Blue)
            ) {
                Text("Clean", style = MyTypography.Body)
            }

            Button(
                onClick = {
                    val selection = StringSelection(output.text)
                    Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, null)
                },
                enabled = output.isNotEmpty(),
                shape = RoundedCornerShape(5.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MyColors.Blue)
            ) {
                Text("Copy", style = MyTypography.Body)
            }
        }
    }
}