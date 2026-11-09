package ph.edu.comteq.cue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                                .clip(RoundedCornerShape(22.dp)),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shadowElevation = 5.dp,
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            ),
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FloatingNavigationItem(
                                    label = "Subjects",
                                    icon = Icons.AutoMirrored.Filled.MenuBook,
                                    selected = currentRoute == "subjects",
                                    onClick = { navController.navigate("subjects") { launchSingleTop = true } }
                                )
                                FloatingNavigationItem(
                                    label = "Review",
                                    icon = Icons.Default.Style,
                                    selected = currentRoute == "review?subjectId={subjectId}",
                                    onClick = { navController.navigate("review") { launchSingleTop = true } }
                                )
                                FloatingNavigationItem(
                                    label = "Progress",
                                    icon = Icons.Default.BarChart,
                                    selected = currentRoute == "progress",
                                    onClick = { navController.navigate("progress") { launchSingleTop = true } }
                                )
                                FloatingNavigationItem(
                                    label = "Settings",
                                    icon = Icons.Default.Settings,
                                    selected = currentRoute == "settings",
                                    onClick = { navController.navigate("settings") { launchSingleTop = true } }
                                )
                            }
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "subjects",
                modifier = Modifier.padding(padding),
                enterTransition = {
                    fadeIn(animationSpec = tween(220)) +
                        slideInHorizontally(animationSpec = tween(220)) { it / 12 }
                },
                exitTransition = { fadeOut(animationSpec = tween(160)) },
                popEnterTransition = { fadeIn(animationSpec = tween(220)) },
                popExitTransition = {
                    fadeOut(animationSpec = tween(160)) +
                        slideOutHorizontally(animationSpec = tween(220)) { -it / 12 }
                }
            ) {
                composable("subjects") {
                    SubjectsScreen(
                        onSubjectClick = { subject ->
                            navController.navigate("subject/${subject.id}")
                        },
                        onReviewClick = { navController.navigate("review") }
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
                            navController.navigate("subject/$subjectId/note?noteId=${note.id}")
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

@Composable
private fun RowScope.FloatingNavigationItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val iconBackground by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = tween(180),
        label = "navigationIconBackground"
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(180),
        label = "navigationIconTint"
    )
    val labelTint by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(180),
        label = "navigationLabelTint"
    )

    Row(
        modifier = Modifier
            .weight(1f)
            .height(52.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(iconBackground)
            .selectable(
                selected = selected,
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab
            )
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(19.dp)
        )
        Text(
            text = label,
            color = labelTint,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                letterSpacing = 0.sp
            ),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            modifier = Modifier.padding(start = 4.dp)
        )
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