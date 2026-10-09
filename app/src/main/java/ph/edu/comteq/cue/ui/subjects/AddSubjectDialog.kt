package ph.edu.comteq.cue.ui.subjects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.clickable

/** The subject icon colors from our mockups. Add more here later if you want. */
val subjectColorKeys = listOf("teal", "coral", "purple", "pink")

fun colorForKey(key: String): Color = when (key) {
    "teal" -> Color(0xFF1D9E75)
    "coral" -> Color(0xFFD85A30)
    "purple" -> Color(0xFF7F77DD)
    "pink" -> Color(0xFFD4537E)
    else -> Color.Gray
}

@Composable
fun AddSubjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, colorKey: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(subjectColorKeys.first()) }
    val isNameBlank = name.isBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add subject") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Subject name") },
                    singleLine = true,
                    isError = isNameBlank && name.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.padding(top = 12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    subjectColorKeys.forEach { key ->
                        ColorDot(
                            colorKey = key,
                            selected = key == selectedColor,
                            onClick = { selectedColor = key }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), selectedColor) },
                enabled = !isNameBlank
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ColorDot(colorKey: String, selected: Boolean, onClick: () -> Unit) {
    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .clickableDot(onClick)
    ) {
        drawCircle(color = colorForKey(colorKey))
        if (selected) {
            drawCircle(color = Color.Black, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
        }
    }
}

private fun Modifier.clickableDot(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)

@Preview(showBackground = true)
@Composable
private fun AddSubjectDialogPreview() {
    MaterialTheme {
        AddSubjectDialog(onDismiss = {}, onConfirm = { _, _ -> })
    }
}