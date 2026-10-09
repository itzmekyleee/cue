package ph.edu.comteq.cue.data.domain.review

import ph.edu.comteq.cue.data.local.entity.Card

object ReviewBoxes {
    const val MIN_BOX_SIZE = 1
    const val MAX_BOX_SIZE = 3

    private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L

    // Returns the number of milliseconds in the given box.
    fun intervalMillis(box: Int): Long = when (box) {
        1 -> 1 * MILLIS_PER_DAY
        2 -> 3 * MILLIS_PER_DAY
        3 -> 7 * MILLIS_PER_DAY
        else -> 1 * MILLIS_PER_DAY
    }

    // Returns a pair of the new box and the new nextReviewDate.
    fun nextBoxAndDate(currentBox: Int, wasCorrect: Boolean, now: Long = System.currentTimeMillis()
    ): Pair<Int, Long> {
        val newBox = if (wasCorrect) {
            (currentBox + 1).coerceAtMost(ReviewBoxes.MAX_BOX_SIZE)
        } else {
            ReviewBoxes.MIN_BOX_SIZE
        }
        val newDate = now + intervalMillis(newBox)
        return Pair(newBox, newDate)
    }

    // Returns the list of cards that are due for review, i.e. their nextReviewDate is less than or equal to now.
    fun dueCards(cards: List<Card>, now: Long = System.currentTimeMillis()): List<Card> =
        cards.filter { it.nextReviewDate <= now }

    fun orderForSession(dueCards: List<Card>): List<Card> {

        // Simple ordering by nextReviewDate (earliest first)
        return dueCards.sortedBy { it.nextReviewDate }
    }
}