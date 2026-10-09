package ph.edu.comteq.cue.data.domain.review

import org.junit.Assert.assertEquals
import org.junit.Test
import ph.edu.comteq.cue.data.local.entity.Card

class ReviewBoxesTest {

    private fun card(box: Int, nextReviewDate: Long) =
        Card(id = 1, noteId = 1, question = "Q", answer = "A", box = box, nextReviewDate = nextReviewDate)

    @Test
    fun correctAnswer_movesCardUpABox() {
        val (newBox, _) = ReviewBoxes.nextBoxAndDate(currentBox = 1, wasCorrect = true, now = 0)
        assertEquals(2, newBox)
    }

    @Test
    fun correctAnswer_capsAtBoxThree() {
        val (newBox, _) = ReviewBoxes.nextBoxAndDate(currentBox = 3, wasCorrect = true, now = 0)
        assertEquals(3, newBox)
    }

    @Test
    fun wrongAnswer_resetsToBoxOne() {
        val (newBox, _) = ReviewBoxes.nextBoxAndDate(currentBox = 3, wasCorrect = false, now = 0)
        assertEquals(1, newBox)
    }

    @Test
    fun nextReviewDate_matchesTheNewBoxInterval() {
        val (newBox, newDate) = ReviewBoxes.nextBoxAndDate(currentBox = 1, wasCorrect = true, now = 0)
        // newBox is 2, and box 2's interval is 3 days
        assertEquals(ReviewBoxes.intervalMillis(newBox), newDate)
    }

    @Test
    fun dueCards_onlyReturnsCardsDueByNow() {
        val now = 10_000L
        val overdue = card(box = 1, nextReviewDate = 1_000)
        val dueNow = card(box = 1, nextReviewDate = now)
        val notDueYet = card(box = 2, nextReviewDate = 20_000)

        val result = ReviewBoxes.dueCards(listOf(overdue, dueNow, notDueYet), now)

        assertEquals(2, result.size)
        assertEquals(true, result.contains(overdue))
        assertEquals(true, result.contains(dueNow))
        assertEquals(false, result.contains(notDueYet))
    }

    @Test
    fun orderForSession_putsOldestDueDateFirst() {
        val newest = card(box = 1, nextReviewDate = 3_000)
        val oldest = card(box = 1, nextReviewDate = 1_000)
        val middle = card(box = 1, nextReviewDate = 2_000)

        val result = ReviewBoxes.orderForSession(listOf(newest, oldest, middle))

        assertEquals(oldest, result[0])
        assertEquals(middle, result[1])
        assertEquals(newest, result[2])
    }
}