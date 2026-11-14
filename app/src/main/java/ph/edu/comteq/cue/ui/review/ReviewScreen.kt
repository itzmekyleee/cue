package ph.edu.comteq.cue.ui.review

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ph.edu.comteq.cue.data.local.entity.Card
import ph.edu.comteq.cue.ui.components.LoadingState
import ph.edu.comteq.cue.ui.components.NavigationPageHeader

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
    val isLoading by viewModel.isLoading.collectAsState()

    ReviewScreenContent(
        totalCards = queue.size,
        currentIndex = currentIndex,
        currentCard = currentCard,
        isFlipped = isFlipped,
        isSessionComplete = isSessionComplete,
        sessionCorrect = sessionCorrect,
        sessionTotal = sessionTotal,
        isLoading = isLoading,
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
    isLoading: Boolean = false,
    onBackClick: () -> Unit,
    onFlip: () -> Unit = {},
    onAnswer: (wasCorrect: Boolean) -> Unit = {}
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { NavigationPageHeader(title = "Review", onBackClick = onBackClick) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                isLoading -> LoadingState()
                totalCards == 0 -> NothingDueState()
                isSessionComplete -> SessionSummary(sessionCorrect, sessionTotal)
                currentCard != null -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Due cards",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${currentIndex + 1} / $totalCards",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { currentIndex.toFloat() / totalCards },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                            .semantics { contentDescription = "Review progress" },
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    FlipCard(
                        card = currentCard,
                        isFlipped = isFlipped,
                        onFlip = onFlip,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    AnimatedContent(
                        targetState = isFlipped,
                        transitionSpec = {
                            fadeIn(tween(150)) togetherWith fadeOut(tween(100))
                        },
                        label = "reviewActions"
                    ) { answerShown ->
                        if (answerShown) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(
                                    onClick = { onAnswer(false) },
                                    modifier = Modifier.weight(1f).height(54.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                    )
                                ) {
                                    Text("Missed")
                                }
                                Button(
                                    onClick = { onAnswer(true) },
                                    modifier = Modifier.weight(1f).height(54.dp)
                                ) {
                                    Text("Got it")
                                }
                            }
                        } else {
                            Button(
                                onClick = onFlip,
                                modifier = Modifier.fillMaxWidth().height(54.dp)
                            ) {
                                Text("Show answer")
                            }
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
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "cardFlip"
    )

    Card(
        onClick = onFlip,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (rotation <= 90f) "QUESTION" else "ANSWER",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = if (rotation > 90f) {
                    Modifier.graphicsLayer { rotationY = 180f }
                } else {
                    Modifier
                }
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (rotation <= 90f) card.question else card.answer,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = if (rotation > 90f) {
                        Modifier
                            .fillMaxWidth()
                            .graphicsLayer { rotationY = 180f }
                    } else {
                        Modifier.fillMaxWidth()
                    }
                )
            }
            Text(
                text = if (isFlipped) "Tap to see question" else "Tap to reveal answer",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = if (rotation > 90f) {
                    Modifier.graphicsLayer { rotationY = 180f }
                } else {
                    Modifier
                }
            )
        }
    }
}

@Composable
private fun SessionSummary(correct: Int, total: Int) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${if (total == 0) 0 else correct * 100 / total}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = "Review complete",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$correct of $total cards remembered",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NothingDueState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "You're all caught up",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "There are no cards due for review right now. Come back when it's time for another round.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
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