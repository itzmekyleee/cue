package ph.edu.comteq.cue.ui.subjects

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ph.edu.comteq.cue.data.local.AppDatabase
import ph.edu.comteq.cue.data.local.entity.Subject
import ph.edu.comteq.cue.data.repository.CardRepository
import ph.edu.comteq.cue.data.repository.SubjectRepository
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import kotlinx.coroutines.launch


/** Talks to the ViewModel. Used by the real app. */
@Composable
fun SubjectsScreen(
    onSubjectClick: (Subject) -> Unit,
    onReviewClick: () -> Unit,
    onSettingsClick: () -> Unit
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
    SubjectsScreenContent(
        subjects = subjects,
        dueCount = totalDueCount,
        onReviewClick = onReviewClick,
        onSettingsClick = onSettingsClick,
        onSubjectClick = onSubjectClick,
        onAddSubject = { name, colorKey -> viewModel.addSubject(name, colorKey) },
        onRenameSubject = { subject, newName -> viewModel.renameSubject(subject, newName) },
        onDeleteSubject = { subject -> viewModel.deleteSubject(subject) },
        onUndoDelete = { subject -> viewModel.undoSubjectDelete(subject) }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreenContent(
    subjects: List<Subject>,
    dueCount: Int = 0,
    onReviewClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onSubjectClick: (Subject) -> Unit,
    onAddSubject: (name: String, colorKey: String) -> Unit,
    onRenameSubject: (subject: Subject, newName: String) -> Unit = { _, _ -> },
    onDeleteSubject: (subject: Subject) -> Unit = {},
    onUndoDelete: (subject: Subject) -> Unit = {}
) {
    var searchText by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var renamingSubject by remember { mutableStateOf<Subject?>(null) }

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add subject")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            ) {
                Text(
                    text = "Subjects",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f)
                )
                ReviewButton(dueCount = dueCount, onClick = onReviewClick)
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Search subjects") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (subjects.isEmpty()) {
                EmptyState()
            } else if (filteredSubjects.isEmpty()) {
                EmptyState(message = "No subjects match \"$searchText\"")
            } else {
                LazyColumn {
                    items(filteredSubjects, key = { it.id }) { subject ->
                        SubjectRow(
                            subject = subject,
                            onClick = { onSubjectClick(subject) },
                            onRenameClick = { renamingSubject = subject },
                            onDeleteClick = { deleteWithUndo(subject) }
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
            onConfirm = { newName ->
                onRenameSubject(subject, newName)
                renamingSubject = null
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
    onDeleteClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(subject.name) },
        trailingContent = {
            Row {
                IconButton(onClick = onRenameClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Rename ${subject.name}")
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete ${subject.name}")
                }
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
    HorizontalDivider()
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