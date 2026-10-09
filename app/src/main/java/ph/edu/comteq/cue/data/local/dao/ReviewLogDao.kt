package ph.edu.comteq.cue.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import ph.edu.comteq.cue.data.local.entity.ReviewLog

@Dao
interface ReviewLogDao {
    @Insert
    suspend fun insert(log: ReviewLog): Long

    @Query("SELECT COUNT(*) FROM review_logs")
    fun countAll(): Flow<Int>

    @Query("SELECT COUNT(*) FROM review_logs WHERE was_correct = 1")
    fun countCorrect(): Flow<Int>

    @Query("SELECT COUNT(*) FROM review_logs WHERE review_at >= :since")
    fun countSince(since: Long): Flow<Int>

    @Query("SELECT review_at FROM review_logs ORDER BY review_at DESC")
    fun getAllReviewTimes(): Flow<List<Long>>

    @Query(
        """
            SELECT COUNT(*) FROM review_logs
            INNER JOIN cards ON review_logs.card_id = cards.id
            INNER JOIN notes ON cards.note_id = notes.id
            WHERE notes.subject_id = :subjectId
        """
    )
    fun countBySubject(subjectId: Long): Flow<Int>

    @Query(
        """
            SELECT COUNT(*) FROM review_logs
            INNER JOIN cards ON review_logs.card_id = cards.id
            INNER JOIN notes ON cards.note_id = notes.id
            WHERE notes.subject_id = :subjectId
            AND review_logs.was_correct = 1
        """
    )
    fun countCorrectBySubject(subjectId: Long): Flow<Int>
}