package ph.edu.comteq.cue.ui.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ph.edu.comteq.cue.data.local.entity.Note
import ph.edu.comteq.cue.ui.components.LoadingState
import ph.edu.comteq.cue.ui.components.NavigationPageHeader
import ph.edu.comteq.cue.ui.components.SearchField
import ph.edu.comteq.cue.ui.subjects.ReviewButton

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
    onReviewClick: () -> Unit = {},
    onSearchTextChange: (String) -> Unit = {},
    onTagSelected: (String) -> Unit = {},
    onAddNoteClick: () -> Unit = {}
) {
    var searchText by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("") }

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
                LazyColumn {
                    items(notes, key = { it.id }) { note ->
                        NoteRow(
                            note = note,
                            onClick = { onNoteClick(note) }
                        )
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
private fun NoteRow(note: Note, onClick: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(
                text = note.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = note.body,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
    HorizontalDivider()
}

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