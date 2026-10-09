package ph.edu.comteq.cue.ui.progress

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressViewModelTest {

    private val oneDayMs = 24L * 60L * 60L * 1000L

    @Test
    fun noReviews_streakIsZero() {
        val streak = calculateStreak(emptyList(), now = 10_000_000L)
        assertEquals(0, streak)
    }

    @Test
    fun reviewedToday_streakIsOne() {
        val now = 10 * oneDayMs
        val reviewedToday = now
        val streak = calculateStreak(listOf(reviewedToday), now)
        assertEquals(1, streak)
    }

    @Test
    fun reviewedTodayAndYesterday_streakIsTwo() {
        val now = 10 * oneDayMs
        val reviewedToday = now
        val reviewedYesterday = now - oneDayMs
        val streak = calculateStreak(listOf(reviewedToday, reviewedYesterday), now)
        assertEquals(2, streak)
    }

    @Test
    fun missedYesterday_streakResetsAtToday() {
        val now = 10 * oneDayMs
        val reviewedToday = now
        val reviewedThreeDaysAgo = now - (3 * oneDayMs)
        // there's a gap on day-1 and day-2, so the streak only counts today
        val streak = calculateStreak(listOf(reviewedToday, reviewedThreeDaysAgo), now)
        assertEquals(1, streak)
    }

    @Test
    fun notReviewedYetToday_butReviewedYesterday_streakStillCounts() {
        val now = 10 * oneDayMs
        val reviewedYesterday = now - oneDayMs
        val reviewedDayBefore = now - (2 * oneDayMs)
        // nothing logged today yet, but yesterday's streak should still count
        val streak = calculateStreak(listOf(reviewedYesterday, reviewedDayBefore), now)
        assertEquals(2, streak)
    }

    @Test
    fun notReviewedTodayOrYesterday_streakIsZero() {
        val now = 10 * oneDayMs
        val reviewedThreeDaysAgo = now - (3 * oneDayMs)
        val streak = calculateStreak(listOf(reviewedThreeDaysAgo), now)
        assertEquals(0, streak)
    }

    @Test
    fun multipleReviewsSameDay_onlyCountsOnceTowardStreak() {
        val now = 10 * oneDayMs + (12 * 60 * 60 * 1000L) // noon on day 10, not midnight
        val review1 = now
        val review2 = now - 1000 // same day, a bit earlier
        val review3 = now - 2000 // same day, even earlier
        val streak = calculateStreak(listOf(review1, review2, review3), now)
        assertEquals(1, streak)
    }

    @Test
    fun accuracyPercent_noReviews_isZero() {
        val accuracy = calculateAccuracyPercent(total = 0, correct = 0)
        assertEquals(0, accuracy)
    }

    @Test
    fun accuracyPercent_allCorrect_isOneHundred() {
        val accuracy = calculateAccuracyPercent(total = 10, correct = 10)
        assertEquals(100, accuracy)
    }

    @Test
    fun accuracyPercent_halfCorrect_isFifty() {
        val accuracy = calculateAccuracyPercent(total = 4, correct = 2)
        assertEquals(50, accuracy)
    }
}