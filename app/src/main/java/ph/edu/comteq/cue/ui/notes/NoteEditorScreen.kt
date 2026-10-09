package ph.edu.comteq.cue.ui.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ph.edu.comteq.cue.data.local.entity.Card
import androidx.compose.foundation.layout.Spacer

/** Talks to the ViewModel. Used by the real app. */
@Composable
fun NoteEditorScreen(
    viewModel: NoteEditorViewModel,
    onBackClick: () -> Unit
) {
    val title by viewModel.title.collectAsState()
    val body by viewModel.body.collectAsState()
    val tagsText by viewModel.tagsText.collectAsState()
    val cards by viewModel.cards.collectAsState()
    val hasUnsavedChanges by viewModel.hasUnsavedChanges.collectAsState()

    NoteEditorScreenContent(
        title = title,
        body = body,
        tagsText = tagsText,
        cards = cards,
        hasUnsavedChanges = hasUnsavedChanges,
        onTitleChange = { viewModel.onTitleChange(it) },
        onBodyChange = { viewModel.onBodyChange(it) },
        onTagsTextChange = { viewModel.onTagsTextChange(it) },
        onSave = { viewModel.save() },
        onAddCard = { question, answer -> viewModel.addCard(question, answer) },
        onUpdateCard = { viewModel.updateCard(it) },
        onDeleteCard = { viewModel.deleteCard(it) },
        onBackClick = onBackClick
    )
}

/** Pure UI, no ViewModel. This is the one @Preview can show. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreenContent(
    title: String,
    body: String,
    tagsText: String,
    cards: List<Card>,
    hasUnsavedChanges: Boolean,
    onTitleChange: (String) -> Unit = {},
    onBodyChange: (String) -> Unit = {},
    onTagsTextChange: (String) -> Unit = {},
    onSave: () -> Unit = {},
    onAddCard: (question: String, answer: String) -> Unit = { _, _ -> },
    onUpdateCard: (Card) -> Unit = {},
    onDeleteCard: (Card) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showAddCardDialog by remember { mutableStateOf(false) }
    var editingCard by remember { mutableStateOf<Card?>(null) }

    fun handleBackClick() {
        if (hasUnsavedChanges) {
            showUnsavedDialog = true
        } else {
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { handleBackClick() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onSave) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                placeholder = { Text("Title") },
                singleLine = true,
                textStyle = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = body,
                onValueChange = onBodyChange,
                placeholder = { Text("Write your note...") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = tagsText,
                onValueChange = onTagsTextChange,
                placeholder = { Text("tags, separated, by, commas") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "CARDS", style = MaterialTheme.typography.labelMedium)

            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cards, key = { it.id }) { card ->
                    CardRow(
                        card = card,
                        onEditClick = { editingCard = card },
                        onDeleteClick = { onDeleteCard(card) }
                    )
                }
            }

            OutlinedButton(
                onClick = { showAddCardDialog = true },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add card")
            }
        }
    }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text("Unsaved changes") },
            text = { Text("Leave without saving your changes?") },
            confirmButton = {
                TextButton(onClick = {
                    showUnsavedDialog = false
                    onBackClick()
                }) {
                    Text("Leave")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedDialog = false }) {
                    Text("Keep editing")
                }
            }
        )
    }

    if (showAddCardDialog) {
        AddCardDialog(
            existingCard = null,
            onDismiss = { showAddCardDialog = false },
            onConfirm = { question, answer ->
                onAddCard(question, answer)
                showAddCardDialog = false
            }
        )
    }

    editingCard?.let { card ->
        AddCardDialog(
            existingCard = card,
            onDismiss = { editingCard = null },
            onConfirm = { question, answer ->
                onUpdateCard(card.copy(question = question, answer = answer))
                editingCard = null
            }
        )
    }
}

@Composable
private fun CardRow(card: Card, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    ListItem(
        headlineContent = { Text("Q: ${card.question}") },
        supportingContent = { Text("A: ${card.answer}") },
        trailingContent = {
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit card")
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete card")
                }
            }
        }
    )
    HorizontalDivider()
}

// ---- Previews: fake data, no ViewModel, no app running ----

@Preview(showBackground = true)
@Composable
private fun NoteEditorScreenPreview() {
    MaterialTheme {
        NoteEditorScreenContent(
            title = "Photosynthesis",
            body = "Plants use sunlight, water, and air to make their own food.",
            tagsText = "biology,cells",
            cards = listOf(
                Card(id = 1, noteId = 1, question = "What does chlorophyll do?", answer = "It absorbs sunlight"),
                Card(id = 2, noteId = 1, question = "Where does it happen?", answer = "In the chloroplasts")
            ),
            hasUnsavedChanges = false
        )
    }
}

@Preview(showBackground = true, name = "New, empty note")
@Composable
private fun NoteEditorScreenEmptyPreview() {
    MaterialTheme {
        NoteEditorScreenContent(
            title = "",
            body = "",
            tagsText = "",
            cards = emptyList(),
            hasUnsavedChanges = false
        )
    }
}