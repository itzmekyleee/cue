package ph.edu.comteq.cue.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ph.edu.comteq.cue.data.repository.CardRepository
import ph.edu.comteq.cue.data.repository.ReviewRepository
import java.util.Calendar

data class ProgressUiState(
    val reviewedToday: Int = 0,
    val accuracyPercent: Int = 0,
    val streakDays: Int = 0,
    val boxCounts: Map<Int, Int> = emptyMap()
)

class ProgressViewModel(
    private val cardRepository: CardRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private fun startOfToday(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private val reviewedToday = reviewRepository.reviewedSince(startOfToday())

    private val accuracyPercent = combine(
        reviewRepository.totalReviews,
        reviewRepository.correctReviews
    ) { total, correct ->
        calculateAccuracyPercent(total, correct)
    }

    private val streakDays = reviewRepository.reviewTimes.map { times ->
        calculateStreak(times)
    }

    private val boxCounts = combine(
        cardRepository.countInBox(1),
        cardRepository.countInBox(2),
        cardRepository.countInBox(3)
    ) { box1, box2, box3 ->
        mapOf(1 to box1, 2 to box2, 3 to box3)
    }

    val uiState: StateFlow<ProgressUiState> = combine(
        reviewedToday, accuracyPercent, streakDays, boxCounts
    ) { today, accuracy, streak, boxes ->
        ProgressUiState(
            reviewedToday = today,
            accuracyPercent = accuracy,
            streakDays = streak,
            boxCounts = boxes
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProgressUiState()
    )
}

/**
 * Counts how many days in a row (ending today or yesterday) have at least one review.
 * This is real Kotlin logic, not a database query — easy to unit test on its own.
 */
fun calculateStreak(reviewTimesMillis: List<Long>, now: Long = System.currentTimeMillis()): Int {
    if (reviewTimesMillis.isEmpty()) return 0

    val dayNumbers = reviewTimesMillis.map { it / (24L * 60L * 60L * 1000L) }.toSet()
    val today = now / (24L * 60L * 60L * 1000L)

    var streak = 0
    var day = today

    // If nothing was reviewed today, the streak can still count up to yesterday.
    if (!dayNumbers.contains(today)) {
        day -= 1
    }

    while (dayNumbers.contains(day)) {
        streak++
        day -= 1
    }

    return streak
}

/** Correct reviews as a percentage of total reviews. 0 if nothing's been reviewed yet. */
fun calculateAccuracyPercent(total: Int, correct: Int): Int =
    if (total == 0) 0 else (correct * 100) / total