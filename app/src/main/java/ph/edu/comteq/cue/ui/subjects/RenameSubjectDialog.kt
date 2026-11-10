package ph.edu.comteq.cue.ui.subjects

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ph.edu.comteq.cue.data.local.entity.Subject

@Composable
fun RenameSubjectDialog(
    subject: Subject,
    onDismiss: () -> Unit,
    onConfirm: (newName: String, colorKey: String) -> Unit
) {
    var name by remember { mutableStateOf(subject.name) }
    var selectedColor by remember { mutableStateOf(subject.colorKey) }
    var nameTouched by remember { mutableStateOf(false) }
    val isNameBlank = name.isBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename subject") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameTouched = true
                    },
                    placeholder = { Text("Subject name") },
                    singleLine = true,
                    isError = nameTouched && isNameBlank,
                    supportingText = if (nameTouched && isNameBlank) {
                        { Text("Subject name can't be blank") }
                    } else {
                        null
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.padding(top = 12.dp))
                SubjectColorPicker(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), selectedColor) },
                enabled = !isNameBlank
            ) {
                Text("Save")
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun RenameSubjectDialogPreview() {
    MaterialTheme {
        RenameSubjectDialog(
            subject = Subject(id = 1, name = "Biology", colorKey = "teal"),
            onDismiss = {},
            onConfirm = { _, _ -> }
        )
    }
}