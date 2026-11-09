package ph.edu.comteq.cue.ui.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.window.Dialog
import ph.edu.comteq.cue.data.local.entity.Note
import ph.edu.comteq.cue.ui.components.LoadingState
import ph.edu.comteq.cue.ui.components.NavigationPageHeader
import ph.edu.comteq.cue.ui.components.SearchField
import ph.edu.comteq.cue.ui.subjects.ReviewButton
import kotlinx.coroutines.delay

@Composable
fun NotesListScreen(
    viewModel: NotesListViewModel,
    onBackClick: () -> Unit,
    onNoteClick: (Note) -> Unit,
    onAddNoteClick: () -> Unit,
    onReviewClick: () -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val dueCount by viewModel.dueCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    NotesListScreenContent(
        notes = notes,
        tags = tags,
        dueCount = dueCount,
        isLoading = isLoading,
        onBackClick = onBackClick,
        onReviewClick = onReviewClick,
        onNoteClick = onNoteClick,
        onDeleteNote = { viewModel.deleteNote(it) },
        onSearchTextChange = { viewModel.onSearchTextChange(it) },
        onTagSelected = { viewModel.onTagSelected(it) },
        onAddNoteClick = onAddNoteClick
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreenContent(
    notes: List<Note>,
    tags: List<String>,
    dueCount: Int = 0,
    isLoading: Boolean = false,
    onBackClick: () -> Unit,
    onNoteClick: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit = {},
    onReviewClick: () -> Unit = {},
    onSearchTextChange: (String) -> Unit = {},
    onTagSelected: (String) -> Unit = {},
    onAddNoteClick: () -> Unit = {}
) {
    var searchText by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("") }
    var pendingDeletion by remember { mutableStateOf<Note?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(showDeleteConfirmation) {
        if (!showDeleteConfirmation) {
            delay(180)
            pendingDeletion = null
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNoteClick) {
                Icon(Icons.Default.Add, contentDescription = "Add note")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            NavigationPageHeader(title = "Notes", onBackClick = onBackClick) {
                ReviewButton(dueCount = dueCount, onClick = onReviewClick)
            }

            SearchField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    onSearchTextChange(it)
                },
                placeholder = "Search notes",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                item {
                    TagChip(
                        label = "All",
                        selected = selectedTag.isEmpty(),
                        onClick = {
                            selectedTag = ""
                            onTagSelected("")
                        }
                    )
                }
                items(tags) { tag ->
                    TagChip(
                        label = tag,
                        selected = selectedTag == tag,
                        onClick = {
                            selectedTag = tag
                            onTagSelected(tag)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                LoadingState()
            } else if (notes.isEmpty() && searchText.isBlank() && selectedTag.isEmpty()) {
                EmptyNotesState()
            } else if (notes.isEmpty()) {
                EmptyNotesState(message = "No notes match your search")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 8.dp,
                        end = 16.dp,
                        bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteRow(
                            note = note,
                            onClick = { onNoteClick(note) },
                            onEditClick = { onNoteClick(note) },
                            onDeleteClick = {
                                pendingDeletion = note
                                showDeleteConfirmation = true
                            },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }

        pendingDeletion?.let { note ->
            Dialog(
                onDismissRequest = { showDeleteConfirmation = false }
            ) {
                AnimatedVisibility(
                    visible = showDeleteConfirmation,
                    enter = fadeIn(tween(180)) + scaleIn(
                        initialScale = 0.92f,
                        animationSpec = tween(180)
                    ),
                    exit = fadeOut(tween(150)) + scaleOut(
                        targetScale = 0.96f,
                        animationSpec = tween(150)
                    )
                ) {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        tonalElevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                            Text(
                                text = "Delete note?",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "\"${note.title.ifBlank { "Untitled note" }}\" will be permanently deleted.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
                            ) {
                                TextButton(
                                    onClick = { showDeleteConfirmation = false }
                                ) {
                                    Text("Cancel")
                                }
                                Button(
                                    onClick = {
                                        onDeleteNote(note)
                                        showDeleteConfirmation = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    )
                                ) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TagChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}

@Composable
private fun NoteRow(
    note: Note,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (note.body.isNotBlank()) {
                    Text(
                        text = note.body.take(NOTE_PREVIEW_MAX_LENGTH).let { preview ->
                            if (note.body.length > NOTE_PREVIEW_MAX_LENGTH) "$preview..." else preview
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable(onClick = onEditClick, role = Role.Button),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit ${note.title.ifBlank { "note" }}",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .clickable(onClick = onDeleteClick, role = Role.Button),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete ${note.title.ifBlank { "note" }}",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

private const val NOTE_PREVIEW_MAX_LENGTH = 180

@Composable
private fun EmptyNotesState(message: String = "No notes yet") {
    Box(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyMedium)
    }
}

// ---- Previews: fake data, no ViewModel, no app running ----

@Preview(showBackground = true)
@Composable
private fun NotesListScreenPreview() {
    MaterialTheme {
        NotesListScreenContent(
            notes = listOf(
                Note(id = 1, subjectId = 1, title = "Photosynthesis", body = "Plants use sunlight, water, and air...", tags = "cells"),
                Note(id = 2, subjectId = 1, title = "Cell structure", body = "Mitochondria, nucleus, ribosomes...", tags = "cells"),
                Note(id = 3, subjectId = 1, title = "DNA replication", body = "Helicase unwinds the double strand...", tags = "genetics")
            ),
            tags = listOf("cells", "genetics", "exam"),
            onBackClick = {},
            onNoteClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty state")
@Composable
private fun NotesListScreenEmptyPreview() {
    MaterialTheme {
        NotesListScreenContent(
            notes = emptyList(),
            tags = emptyList(),
            onBackClick = {},
            onNoteClick = {}
        )
    }
}