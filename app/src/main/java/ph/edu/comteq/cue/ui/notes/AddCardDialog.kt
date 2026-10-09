package ph.edu.comteq.cue.ui.notes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import ph.edu.comteq.cue.data.local.entity.Card
import androidx.compose.foundation.layout.Spacer

@Composable
fun AddCardDialog(
    existingCard: Card?,
    onDismiss: () -> Unit,
    onConfirm: (question: String, answer: String) -> Unit
) {
    var question by remember { mutableStateOf(existingCard?.question ?: "") }
    var answer by remember { mutableStateOf(existingCard?.answer ?: "") }
    val isInvalid = question.isBlank() || answer.isBlank()
    val title = if (existingCard == null) "Add card" else "Edit card"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    placeholder = { Text("Question") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    placeholder = { Text("Answer") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(question.trim(), answer.trim()) },
                enabled = !isInvalid
            ) {
                Text(if (existingCard == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true, name = "Add card")
@Composable
private fun AddCardDialogPreview() {
    MaterialTheme {
        AddCardDialog(existingCard = null, onDismiss = {}, onConfirm = { _, _ -> })
    }
}

@Preview(showBackground = true, name = "Edit card")
@Composable
private fun EditCardDialogPreview() {
    MaterialTheme {
        AddCardDialog(
            existingCard = Card(id = 1, noteId = 1, question = "What does chlorophyll do?", answer = "It absorbs sunlight"),
            onDismiss = {},
            onConfirm = { _, _ -> }
        )
    }
}