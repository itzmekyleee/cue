package ph.edu.comteq.cue.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/** Talks to the ViewModel. Used by the real app. */
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val theme by viewModel.theme.collectAsState()
    val dailyGoal by viewModel.dailyGoal.collectAsState()
    val sortByTitle by viewModel.sortByTitle.collectAsState()

    SettingsScreenContent(
        theme = theme,
        dailyGoal = dailyGoal,
        sortByTitle = sortByTitle,
        onThemeChange = { viewModel.setTheme(it) },
        onDailyGoalChange = { viewModel.setDailyGoal(it) },
        onSortByTitleChange = { viewModel.setSortByTitle(it) },
        onClearAllData = { viewModel.clearAllData() }
    )
}

/** Pure UI, no ViewModel. This is the one @Preview can show. */
@Composable
fun SettingsScreenContent(
    theme: String,
    dailyGoal: Int,
    sortByTitle: Boolean,
    onThemeChange: (String) -> Unit = {},
    onDailyGoalChange: (Int) -> Unit = {},
    onSortByTitleChange: (Boolean) -> Unit = {},
    onClearAllData: () -> Unit = {}
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

            SettingsGroupLabel("APPEARANCE")
            SettingsRow(
                title = "Theme",
                value = theme.replaceFirstChar { it.uppercase() },
                onClick = { showThemeDialog = true }
            )

            SettingsGroupLabel("REVIEW")
            SettingsRow(
                title = "Daily goal",
                value = "$dailyGoal cards",
                onClick = { showGoalDialog = true }
            )

            SettingsGroupLabel("NOTES")
            SettingsSwitchRow(
                checked = sortByTitle,
                onCheckedChange = onSortByTitleChange
            )

            SettingsGroupLabel("DATA")
            SettingsRow(
                title = "Clear all data",
                value = "",
                isDanger = true,
                onClick = { showClearConfirm = true }
            )
        }
    }

    if (showThemeDialog) {
        ThemePickerDialog(
            currentTheme = theme,
            onDismiss = { showThemeDialog = false },
            onSelect = { selected ->
                onThemeChange(selected)
                showThemeDialog = false
            }
        )
    }

    if (showGoalDialog) {
        DailyGoalDialog(
            currentGoal = dailyGoal,
            onDismiss = { showGoalDialog = false },
            onConfirm = { newGoal ->
                onDailyGoalChange(newGoal)
                showGoalDialog = false
            }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear all data") },
            text = { Text("This deletes every subject, note, and card. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showClearConfirm = false
                    onClearAllData()
                }) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsGroupLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsRow(
    title: String,
    value: String,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                color = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        },
        trailingContent = if (value.isNotEmpty()) {
            { Text(value) }
        } else null,
        modifier = Modifier.clickableRow(onClick)
    )
}

@Composable
private fun SettingsSwitchRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text("Sort by title") },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    )
}

private fun Modifier.clickableRow(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)

// ---- Preview: fake data, no ViewModel, no app running ----

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreenContent(
            theme = "system",
            dailyGoal = 20,
            sortByTitle = false
        )
    }
}