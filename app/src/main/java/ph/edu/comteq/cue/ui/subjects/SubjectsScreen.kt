package ph.edu.comteq.cue.ui.subjects

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ph.edu.comteq.cue.data.local.AppDatabase
import ph.edu.comteq.cue.data.local.entity.Subject
import ph.edu.comteq.cue.data.repository.CardRepository
import ph.edu.comteq.cue.data.repository.SubjectRepository
import ph.edu.comteq.cue.ui.components.LoadingState
import ph.edu.comteq.cue.ui.components.LargePageHeader
import ph.edu.comteq.cue.ui.components.SearchField
import kotlinx.coroutines.launch


/** Talks to the ViewModel. Used by the real app. */
@Composable
fun SubjectsScreen(
    onSubjectClick: (Subject) -> Unit,
    onReviewClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SubjectsViewModel = viewModel {
        val database = AppDatabase.getInstance(context)
        SubjectsViewModel(
            repository = SubjectRepository(database.subjectDao()),
            cardRepository = CardRepository(database.cardDao())
        )
    }
    val subjects by viewModel.subjects.collectAsState()
    val totalDueCount by viewModel.totalDueCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    SubjectsScreenContent(
        subjects = subjects,
        dueCount = totalDueCount,
        isLoading = isLoading,
        onReviewClick = onReviewClick,
        onSubjectClick = onSubjectClick,
        onAddSubject = { name, colorKey -> viewModel.addSubject(name, colorKey) },
        onRenameSubject = { subject, newName, colorKey ->
            viewModel.renameSubject(subject, newName, colorKey)
        },
        onDeleteSubject = { subject -> viewModel.deleteSubject(subject) },
        onUndoDelete = { subject -> viewModel.undoSubjectDelete(subject) }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreenContent(
    subjects: List<Subject>,
    dueCount: Int = 0,
    isLoading: Boolean = false,
    onReviewClick: () -> Unit = {},
    onSubjectClick: (Subject) -> Unit,
    onAddSubject: (name: String, colorKey: String) -> Unit,
    onRenameSubject: (subject: Subject, newName: String, colorKey: String) -> Unit = { _, _, _ -> },
    onDeleteSubject: (subject: Subject) -> Unit = {},
    onUndoDelete: (subject: Subject) -> Unit = {}
) {
    var searchText by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var renamingSubject by remember { mutableStateOf<Subject?>(null) }
    var pendingDeletion by remember { mutableStateOf<Subject?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    var coroutineScope = rememberCoroutineScope()

    val filteredSubjects = remember(subjects, searchText) {
        if (searchText.isBlank()) {
            subjects
        } else {
            subjects.filter { it.name.contains(searchText, ignoreCase = true) }
        }
    }

    fun deleteWithUndo(subject: Subject) {
        onDeleteSubject(subject)
        coroutineScope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "Deleted \"${subject.name}\"",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                onUndoDelete(subject)
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add subject")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LargePageHeader(title = "Subjects") {
                ReviewButton(dueCount = dueCount, onClick = onReviewClick)
            }

            SearchField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = "Search subjects",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                LoadingState()
            } else if (subjects.isEmpty()) {
                EmptyState()
            } else if (filteredSubjects.isEmpty()) {
                EmptyState(message = "No subjects match \"$searchText\"")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 12.dp,
                        end = 16.dp,
                        bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredSubjects, key = { it.id }) { subject ->
                        SubjectRow(
                            subject = subject,
                            onClick = { onSubjectClick(subject) },
                            onRenameClick = { renamingSubject = subject },
                            onDeleteClick = { pendingDeletion = subject },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSubjectDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, colorKey ->
                onAddSubject(name, colorKey)
                showAddDialog = false
            }
        )
    }
    renamingSubject?.let { subject ->
        RenameSubjectDialog(
            subject = subject,
            onDismiss = { renamingSubject = null },
            onConfirm = { newName, colorKey ->
                onRenameSubject(subject, newName, colorKey)
                renamingSubject = null
            }
        )
    }
    pendingDeletion?.let { subject ->
        AlertDialog(
            onDismissRequest = { pendingDeletion = null },
            title = { Text("Delete subject?") },
            text = {
                Text(
                    "\"${subject.name}\" and its notes and cards will be permanently deleted."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteWithUndo(subject)
                        pendingDeletion = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeletion = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ReviewButton(dueCount: Int, onClick: () -> Unit) {
    BadgedBox(
        badge = {
            if (dueCount > 0) {
                Badge { Text("$dueCount") }
            }
        }
    ) {
        FilledTonalButton(onClick = onClick, enabled = dueCount > 0) {
            Text("Review")
        }
    }
}

@Composable
private fun SubjectRow(
    subject: Subject,
    onClick: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDefaultColor = subject.colorKey == DEFAULT_SUBJECT_COLOR_KEY
    val accent = colorForKey(subject.colorKey, MaterialTheme.colorScheme.primary)
    val iconBackground = if (isDefaultColor) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        accent.copy(alpha = 0.14f)
    }

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
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Topic,
                    contentDescription = null,
                    tint = if (isDefaultColor) MaterialTheme.colorScheme.primary else accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = subject.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 8.dp),
                overflow = TextOverflow.Ellipsis
            )
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable(onClick = onRenameClick, role = Role.Button),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Rename ${subject.name}",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .clickable(onClick = onDeleteClick, role = Role.Button),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete ${subject.name}",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyState(message: String = "No subjects yet") {
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
private fun SubjectsScreenPreview() {
    MaterialTheme {
        SubjectsScreenContent(
            subjects = listOf(
                Subject(id = 1, name = "Biology", colorKey = "teal"),
                Subject(id = 2, name = "Calculus", colorKey = "coral"),
                Subject(id = 3, name = "World history", colorKey = "purple")
            ),
            onSubjectClick = {},
            onAddSubject = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "Empty state")
@Composable
private fun SubjectsScreenEmptyPreview() {
    MaterialTheme {
        SubjectsScreenContent(
            subjects = emptyList(),
            onSubjectClick = {},
            onAddSubject = { _, _ -> }
        )
    }
}