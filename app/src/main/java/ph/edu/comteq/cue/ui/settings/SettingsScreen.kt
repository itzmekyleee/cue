package ph.edu.comteq.cue.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ph.edu.comteq.cue.ui.components.LoadingState
import ph.edu.comteq.cue.ui.components.LargePageHeader

/** Talks to the ViewModel. Used by the real app. */
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val theme by viewModel.theme.collectAsState()
    val dailyGoal by viewModel.dailyGoal.collectAsState()
    val sortByTitle by viewModel.sortByTitle.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    SettingsScreenContent(
        theme = theme,
        dailyGoal = dailyGoal,
        sortByTitle = sortByTitle,
        isLoading = isLoading,
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
    isLoading: Boolean = false,
    onThemeChange: (String) -> Unit = {},
    onDailyGoalChange: (Int) -> Unit = {},
    onSortByTitleChange: (Boolean) -> Unit = {},
    onClearAllData: () -> Unit = {}
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }

    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0)) { padding ->
        if (isLoading) {
            LoadingState(modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                LargePageHeader(title = "Settings")
                Text(
                    text = "Make Cue work the way you study.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
                )

                SettingsGroupLabel("Appearance")
                SettingsGroup {
                    SettingsActionRow(
                        icon = Icons.Default.Palette,
                        title = "Theme",
                        description = "Choose light, dark, or follow your device",
                        value = theme.replaceFirstChar { it.uppercase() },
                        onClick = { showThemeDialog = true }
                    )
                }

                SettingsGroupLabel("Study")
                SettingsGroup {
                    SettingsActionRow(
                        icon = Icons.Default.Flag,
                        title = "Daily goal",
                        description = "Set how many cards you want to review",
                        value = "$dailyGoal cards",
                        onClick = { showGoalDialog = true }
                    )
                }

                SettingsGroupLabel("Notes")
                SettingsGroup {
                    SettingsSwitchRow(
                        icon = Icons.Default.SortByAlpha,
                        checked = sortByTitle,
                        onCheckedChange = onSortByTitleChange
                    )
                }

                SettingsGroupLabel("Data")
                SettingsGroup {
                    SettingsActionRow(
                        icon = Icons.Default.DeleteOutline,
                        title = "Clear all data",
                        description = "Remove all subjects, notes, and cards",
                        isDanger = true,
                        onClick = { showClearConfirm = true }
                    )
                }
            }
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
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        )
    ) {
        content()
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    description: String,
    value: String? = null,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SettingsIcon(icon = icon, isDanger = isDanger)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SettingsIcon(icon = icon)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = "Sort by title",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Show notes in alphabetical order",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsIcon(icon: ImageVector, isDanger: Boolean = false) {
    val tint = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val container = if (isDanger) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(container),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(19.dp)
        )
    }
}

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