package ph.edu.comteq.cue.ui.notes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ph.edu.comteq.cue.data.local.entity.Card
import ph.edu.comteq.cue.ui.components.LoadingState
import ph.edu.comteq.cue.ui.components.NavigationPageHeader
import androidx.compose.ui.semantics.Role
import kotlinx.coroutines.launch

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
    val isLoading by viewModel.isLoading.collectAsState()

    NoteEditorScreenContent(
        title = title,
        body = body,
        tagsText = tagsText,
        cards = cards,
        hasUnsavedChanges = hasUnsavedChanges,
        isLoading = isLoading,
        onTitleChange = { viewModel.onTitleChange(it) },
        onBodyChange = { viewModel.onBodyChange(it) },
        onTagsTextChange = { viewModel.onTagsTextChange(it) },
        onSave = { viewModel.save(onSaved = onBackClick) },
        onAddCard = { question, answer -> viewModel.addCard(question, answer) },
        onUpdateCard = { viewModel.updateCard(it) },
        onDeleteCard = { viewModel.deleteCard(it) },
        onRestoreCard = { viewModel.restoreCard(it) },
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
    isLoading: Boolean = false,
    onTitleChange: (String) -> Unit = {},
    onBodyChange: (String) -> Unit = {},
    onTagsTextChange: (String) -> Unit = {},
    onSave: () -> Unit = {},
    onAddCard: (question: String, answer: String) -> Unit = { _, _ -> },
    onUpdateCard: (Card) -> Unit = {},
    onDeleteCard: (Card) -> Unit = {},
    onRestoreCard: (Card) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var showAddCardDialog by remember { mutableStateOf(false) }
    var editingCard by remember { mutableStateOf<Card?>(null) }
    var pendingCardDeletion by remember { mutableStateOf<Card?>(null) }

    fun handleBackClick() {
        if (hasUnsavedChanges) {
            showUnsavedDialog = true
        } else {
            onBackClick()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun deleteCardWithUndo(card: Card) {
        onDeleteCard(card)
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "Card deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                onRestoreCard(card)
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            NavigationPageHeader(
                title = title.ifBlank { "New note" },
                onBackClick = { handleBackClick() },
                actions = {
                    TextButton(onClick = onSave) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            LoadingState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = onTitleChange,
                        label = { Text("Title") },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleLarge,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = body,
                        onValueChange = onBodyChange,
                        label = { Text("Note") },
                        placeholder = { Text("Write down what you want to remember...") },
                        minLines = 5,
                        maxLines = 12,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 150.dp, max = 260.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = tagsText,
                        onValueChange = onTagsTextChange,
                        label = { Text("Tags") },
                        placeholder = { Text("Separate tags with commas") },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Flashcards",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (cards.isEmpty()) {
                                    "Add questions to practice this note"
                                } else {
                                    "${cards.size} ${if (cards.size == 1) "card" else "cards"} ready to review"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (cards.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .padding(horizontal = 20.dp, vertical = 22.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Your flashcards will appear here.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                items(cards, key = { it.id }) { card ->
                    CardRow(
                        card = card,
                        onEditClick = { editingCard = card },
                        onDeleteClick = { pendingCardDeletion = card }
                    )
                }

                item {
                    FilledTonalButton(
                        onClick = { showAddCardDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add flashcard")
                    }
                }
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

    pendingCardDeletion?.let { card ->
        AlertDialog(
            onDismissRequest = { pendingCardDeletion = null },
            title = { Text("Delete flashcard?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = card.question,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 3,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = "This card will be removed from this note. You can undo this briefly after deleting.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteCardWithUndo(card)
                        pendingCardDeletion = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingCardDeletion = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CardRow(card: Card, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Question",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = card.question,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 3,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Answer",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = card.answer,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompactCardAction(
                        icon = Icons.Default.Edit,
                        label = "Edit card",
                        onClick = onEditClick
                    )
                    CompactCardAction(
                        icon = Icons.Default.Delete,
                        label = "Delete card",
                        isDanger = true,
                        onClick = onDeleteClick
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactCardAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    val containerColor = if (isDanger) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val iconColor = if (isDanger) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(containerColor)
            .clickable(onClick = onClick, role = Role.Button),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )
    }
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