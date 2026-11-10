package ph.edu.comteq.cue.ui.subjects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
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
    var nameTouched by remember { mutableStateOf(false) }
    val isNameBlank = name.isBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add subject") },
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

                Spacer(modifier = Modifier.padding(top = 16.dp))
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
fun SubjectColorPicker(selectedColor: String, onColorSelected: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Subject color",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            subjectColorKeys.forEach { key ->
                ColorDot(
                    colorKey = key,
                    selected = key == selectedColor,
                    onClick = { onColorSelected(key) }
                )
            }
        }
    }
}

@Composable
private fun ColorDot(colorKey: String, selected: Boolean, onClick: () -> Unit) {
    val label = colorKey.replaceFirstChar { it.uppercase() }
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = "$label subject color"
                this.selected = selected
                role = Role.RadioButton
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .border(
                    width = if (selected) 2.dp else 0.dp,
                    color = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                    shape = CircleShape
                )
                .padding(4.dp)
                .size(28.dp)
                .background(colorForKey(colorKey), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddSubjectDialogPreview() {
    MaterialTheme {
        AddSubjectDialog(onDismiss = {}, onConfirm = { _, _ -> })
    }
}