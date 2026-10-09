package ph.edu.comteq.cue.ui.progress

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/** Talks to the ViewModel. Used by the real app. */
@Composable
fun ProgressScreen(viewModel: ProgressViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    ProgressScreenContent(uiState = uiState)
}

/** Pure UI, no ViewModel. This is the one @Preview can show. */
@Composable
fun ProgressScreenContent(uiState: ProgressUiState) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(text = "Progress", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            StreakCard(streakDays = uiState.streakDays)

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = "Reviewed today",
                    value = uiState.reviewedToday.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Accuracy",
                    value = "${uiState.accuracyPercent}%",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "CARDS BY BOX", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))

            BoxCountRow(label = "Box 1", count = uiState.boxCounts[1] ?: 0)
            BoxCountRow(label = "Box 2", count = uiState.boxCounts[2] ?: 0)
            BoxCountRow(label = "Box 3", count = uiState.boxCounts[3] ?: 0)
        }
    }
}

@Composable
private fun StreakCard(streakDays: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "$streakDays day${if (streakDays == 1) "" else "s"}", style = MaterialTheme.typography.headlineSmall)
            Text(text = "Current streak", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, style = MaterialTheme.typography.bodySmall)
            Text(text = value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun BoxCountRow(label: String, count: Int) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = count.toString(), style = MaterialTheme.typography.bodyMedium)
    }
}

// ---- Preview: fake data, no ViewModel, no app running ----

@Preview(showBackground = true)
@Composable
private fun ProgressScreenPreview() {
    MaterialTheme {
        ProgressScreenContent(
            uiState = ProgressUiState(
                reviewedToday = 14,
                accuracyPercent = 82,
                streakDays = 6,
                boxCounts = mapOf(1 to 18, 2 to 10, 3 to 5)
            )
        )
    }
}

@Preview(showBackground = true, name = "No activity yet")
@Composable
private fun ProgressScreenEmptyPreview() {
    MaterialTheme {
        ProgressScreenContent(uiState = ProgressUiState())
    }
}