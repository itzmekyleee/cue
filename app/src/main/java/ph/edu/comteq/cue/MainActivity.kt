package ph.edu.comteq.cue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ph.edu.comteq.cue.data.local.AppDatabase
import ph.edu.comteq.cue.data.datastore.SettingsDataStore
import ph.edu.comteq.cue.data.repository.CardRepository
import ph.edu.comteq.cue.data.repository.NoteRepository
import ph.edu.comteq.cue.data.repository.ReviewRepository
import ph.edu.comteq.cue.data.repository.SubjectRepository
import ph.edu.comteq.cue.ui.notes.NoteEditorScreen
import ph.edu.comteq.cue.ui.notes.NoteEditorViewModel
import ph.edu.comteq.cue.ui.notes.NotesListScreen
import ph.edu.comteq.cue.ui.notes.NotesListViewModel
import ph.edu.comteq.cue.ui.review.ReviewScreen
import ph.edu.comteq.cue.ui.review.ReviewViewModel
import ph.edu.comteq.cue.ui.settings.SettingsScreen
import ph.edu.comteq.cue.ui.settings.SettingsViewModel
import ph.edu.comteq.cue.ui.subjects.SubjectsScreen
import ph.edu.comteq.cue.ui.subjects.SubjectsScreenContent
import ph.edu.comteq.cue.ui.theme.CueTheme
import ph.edu.comteq.cue.ui.progress.ProgressScreen
import ph.edu.comteq.cue.ui.progress.ProgressViewModel
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Cue()
        }
    }
}

@Composable
fun Cue() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val database = remember(context) { AppDatabase.getInstance(context) }
    val settingsDataStore = remember(context) { SettingsDataStore(context) }
    val savedTheme by settingsDataStore.theme.collectAsState(initial = "system")
    val useDarkTheme = when (savedTheme) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    CueTheme(darkTheme = useDarkTheme) {
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route
        val topLevelRoutes = setOf("subjects", "review?subjectId={subjectId}", "progress", "settings")

        Scaffold(
            bottomBar = {
                if (currentRoute in topLevelRoutes) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentRoute == "subjects",
                            onClick = { navController.navigate("subjects") { launchSingleTop = true } },
                            icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Subjects") },
                            label = { Text("Subjects") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == "review?subjectId={subjectId}",
                            onClick = { navController.navigate("review") { launchSingleTop = true } },
                            icon = { Icon(Icons.Default.Style, contentDescription = "Review") },
                            label = { Text("Review") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == "progress",
                            onClick = { navController.navigate("progress") { launchSingleTop = true } },
                            icon = { Icon(Icons.Default.BarChart, contentDescription = "Progress") },
                            label = { Text("Progress") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == "settings",
                            onClick = { navController.navigate("settings") { launchSingleTop = true } },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                            label = { Text("Settings") }
                        )
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "subjects",
                modifier = Modifier.padding(padding)
            ) {
                composable("subjects") {
                    SubjectsScreen(
                        onSubjectClick = { subject ->
                            navController.navigate("subject/${subject.id}")
                        },
                        onReviewClick = { navController.navigate("review") },
                        onSettingsClick = { navController.navigate("settings") }
                    )
                }
                composable("settings") {
                    val viewModel: SettingsViewModel = viewModel {
                        SettingsViewModel(
                            settingsDataStore,
                            SubjectRepository(database.subjectDao())
                        )
                    }
                    SettingsScreen(viewModel = viewModel)
                }
                composable(
                    route = "review?subjectId={subjectId}",
                    arguments = listOf(
                        navArgument("subjectId") {
                            type = NavType.LongType
                            defaultValue = NO_SUBJECT_ID
                        }
                    )
                ) { backStackEntry ->
                    val subjectId = backStackEntry.arguments?.getLong("subjectId")
                        ?.takeIf { it != NO_SUBJECT_ID }
                    val viewModel: ReviewViewModel = viewModel {
                        ReviewViewModel(
                            subjectId = subjectId,
                            cardRepository = CardRepository(database.cardDao()),
                            reviewRepository = ReviewRepository(database.reviewLogDao())
                        )
                    }
                    ReviewScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "subject/{subjectId}",
                    arguments = listOf(navArgument("subjectId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val subjectId = backStackEntry.arguments?.getLong("subjectId") ?: return@composable
                    val viewModel: NotesListViewModel = viewModel {
                        NotesListViewModel(
                            subjectId = subjectId,
                            repository = NoteRepository(database.noteDao()),
                            cardRepository = CardRepository(database.cardDao()),
                            settingsDataStore = settingsDataStore
                        )
                    }
                    NotesListScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() },
                        onNoteClick = { note ->
                            navController.navigate("subject/$subjectId/note/${note.id}")
                        },
                        onAddNoteClick = {
                            navController.navigate("subject/$subjectId/note")
                        },
                        onReviewClick = {
                            navController.navigate("review?subjectId=$subjectId")
                        }
                    )
                }
                composable(
                    route = "subject/{subjectId}/note?noteId={noteId}",
                    arguments = listOf(
                        navArgument("subjectId") { type = NavType.LongType },
                        navArgument("noteId") {
                            type = NavType.LongType
                            defaultValue = NO_NOTE_ID
                        }
                    )
                ) { backStackEntry ->
                    val subjectId = backStackEntry.arguments?.getLong("subjectId") ?: return@composable
                    val noteId = backStackEntry.arguments?.getLong("noteId")
                        ?.takeIf { it != NO_NOTE_ID }
                    val viewModel: NoteEditorViewModel = viewModel {
                        NoteEditorViewModel(
                            subjectId = subjectId,
                            initialNoteId = noteId,
                            noteRepository = NoteRepository(database.noteDao()),
                            cardRepository = CardRepository(database.cardDao())
                        )
                    }
                    NoteEditorScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable("progress") {
                    val viewModel: ProgressViewModel = viewModel {
                        ProgressViewModel(
                            cardRepository = CardRepository(database.cardDao()),
                            reviewRepository = ReviewRepository(database.reviewLogDao())
                        )
                    }
                    ProgressScreen(viewModel = viewModel)
                }
            }
        }
    }
}


private const val NO_NOTE_ID = -1L
private const val NO_SUBJECT_ID = -1L

@Preview(showBackground = true)
@Composable
fun CuePreview() {
    CueTheme {
        SubjectsScreenContent(
            subjects = emptyList(),
            onSubjectClick = {},
            onAddSubject = { _, _ -> }
        )
    }
}