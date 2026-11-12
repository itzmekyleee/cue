package ph.edu.comteq.cue.ui.notes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun AddCardDialog(
    existingCard: Card?,
    onDismiss: () -> Unit,
    onConfirm: (question: String, answer: String) -> Unit
) {
    var question by remember { mutableStateOf(existingCard?.question ?: "") }
    var answer by remember { mutableStateOf(existingCard?.answer ?: "") }
    var questionTouched by remember { mutableStateOf(false) }
    var answerTouched by remember { mutableStateOf(false) }
    val isInvalid = question.isBlank() || answer.isBlank()
    val title = if (existingCard == null) "New flashcard" else "Edit flashcard"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = "Write a prompt, then the answer you want to remember.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = question,
                    onValueChange = {
                        question = it
                        questionTouched = true
                    },
                    label = { Text("Question") },
                    placeholder = { Text("Ask something specific") },
                    isError = questionTouched && question.isBlank(),
                    supportingText = if (questionTouched && question.isBlank()) {
                        { Text("Question can't be blank") }
                    } else {
                        null
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
                OutlinedTextField(
                    value = answer,
                    onValueChange = {
                        answer = it
                        answerTouched = true
                    },
                    label = { Text("Answer") },
                    placeholder = { Text("Add a clear, concise answer") },
                    isError = answerTouched && answer.isBlank(),
                    supportingText = if (answerTouched && answer.isBlank()) {
                        { Text("Answer can't be blank") }
                    } else {
                        null
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(question.trim(), answer.trim()) },
                enabled = !isInvalid
            ) {
                Text(if (existingCard == null) "Add card" else "Save changes")
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