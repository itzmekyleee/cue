package ph.edu.comteq.cue.ui.review

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ph.edu.comteq.cue.data.local.entity.Card
import ph.edu.comteq.cue.data.local.entity.Card as CardEntity

/** Talks to the ViewModel. Used by the real app. */
@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel,
    onBackClick: () -> Unit
) {
    val queue by viewModel.queue.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val currentCard by viewModel.currentCard.collectAsState()
    val isFlipped by viewModel.isFlipped.collectAsState()
    val isSessionComplete by viewModel.isSessionComplete.collectAsState()
    val sessionCorrect by viewModel.sessionCorrect.collectAsState()
    val sessionTotal by viewModel.sessionTotal.collectAsState()

    ReviewScreenContent(
        totalCards = queue.size,
        currentIndex = currentIndex,
        currentCard = currentCard,
        isFlipped = isFlipped,
        isSessionComplete = isSessionComplete,
        sessionCorrect = sessionCorrect,
        sessionTotal = sessionTotal,
        onBackClick = onBackClick,
        onFlip = { viewModel.flipCard() },
        onAnswer = { wasCorrect -> viewModel.answer(wasCorrect) }
    )
}

/** Pure UI, no ViewModel. This is the one @Preview can show. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreenContent(
    totalCards: Int,
    currentIndex: Int,
    currentCard: Card?,
    isFlipped: Boolean,
    isSessionComplete: Boolean,
    sessionCorrect: Int,
    sessionTotal: Int,
    onBackClick: () -> Unit,
    onFlip: () -> Unit = {},
    onAnswer: (wasCorrect: Boolean) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                totalCards == 0 -> NothingDueState()
                isSessionComplete -> SessionSummary(sessionCorrect, sessionTotal)
                currentCard != null -> {
                    LinearProgressIndicator(
                        progress = { (currentIndex + 1f) / totalCards },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Card ${currentIndex + 1} of $totalCards",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    FlipCard(
                        card = currentCard,
                        isFlipped = isFlipped,
                        onFlip = onFlip,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isFlipped) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { onAnswer(false) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Missed")
                            }
                            Button(
                                onClick = { onAnswer(true) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Got it")
                            }
                        }
                    } else {
                        Button(
                            onClick = onFlip,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Show answer")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlipCard(
    card: Card,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onFlip,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isFlipped) card.answer else card.question,
                style = MaterialTheme.typography.titleLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun SessionSummary(correct: Int, total: Int) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("All done for today", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text("$correct of $total correct", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun NothingDueState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Nothing due right now", style = MaterialTheme.typography.bodyLarge)
    }
}

// ---- Previews: fake data, no ViewModel, no app running ----

@Preview(showBackground = true, name = "Card showing question")
@Composable
private fun ReviewScreenQuestionPreview() {
    MaterialTheme {
        ReviewScreenContent(
            totalCards = 5,
            currentIndex = 1,
            currentCard = Card(id = 1, noteId = 1, question = "What does chlorophyll do?", answer = "It absorbs sunlight"),
            isFlipped = false,
            isSessionComplete = false,
            sessionCorrect = 1,
            sessionTotal = 1,
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Card showing answer")
@Composable
private fun ReviewScreenAnswerPreview() {
    MaterialTheme {
        ReviewScreenContent(
            totalCards = 5,
            currentIndex = 1,
            currentCard = Card(id = 1, noteId = 1, question = "What does chlorophyll do?", answer = "It absorbs sunlight"),
            isFlipped = true,
            isSessionComplete = false,
            sessionCorrect = 1,
            sessionTotal = 1,
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Session complete")
@Composable
private fun ReviewScreenCompletePreview() {
    MaterialTheme {
        ReviewScreenContent(
            totalCards = 5,
            currentIndex = 5,
            currentCard = null,
            isFlipped = false,
            isSessionComplete = true,
            sessionCorrect = 4,
            sessionTotal = 5,
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Nothing due")
@Composable
private fun ReviewScreenEmptyPreview() {
    MaterialTheme {
        ReviewScreenContent(
            totalCards = 0,
            currentIndex = 0,
            currentCard = null,
            isFlipped = false,
            isSessionComplete = false,
            sessionCorrect = 0,
            sessionTotal = 0,
            onBackClick = {}
        )
    }
}