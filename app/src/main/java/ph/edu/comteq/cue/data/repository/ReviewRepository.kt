package ph.edu.comteq.cue.data.repository

import kotlinx.coroutines.flow.Flow
import ph.edu.comteq.cue.data.local.dao.ReviewLogDao
import ph.edu.comteq.cue.data.local.entity.ReviewLog

class ReviewRepository(private val dao: ReviewLogDao) {

    val totalReviews: Flow<Int> = dao.countAll()
    val correctReviews: Flow<Int> = dao.countCorrect()
    val reviewTimes: Flow<List<Long>> = dao.getAllReviewTimes()

    /** Saves one review. Call this every time you tap Got it or Missed. */
    suspend fun log(cardId: Long, wasCorrect: Boolean): Long =
        dao.insert(ReviewLog(cardId = cardId, wasCorrect = wasCorrect))

    fun reviewedSince(since: Long): Flow<Int> = dao.countSince(since)

    fun totalForSubject(subjectId: Long): Flow<Int> = dao.countBySubject(subjectId)

    fun correctForSubject(subjectId: Long): Flow<Int> = dao.countCorrectBySubject(subjectId)
}