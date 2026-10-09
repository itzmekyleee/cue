package ph.edu.comteq.cue.data.local.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow
import ph.edu.comteq.cue.data.local.entity.Card

@Dao
interface CardDao {
    @Insert
    suspend fun insert(card: Card): Long

    @Update
    suspend fun update(card: Card)

    @Delete
    suspend fun delete(card: Card)

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getById(id: Long): Card?

    @Query("SELECT * FROM cards WHERE note_id = :noteId ORDER BY id ASC")
    fun getByNote(noteId: Long): Flow<List<Card>>

    @Query("SELECT * FROM cards WHERE next_review_date <= :now ORDER BY next_review_date ASC")
    suspend fun getDueCards(now: Long): List<Card>

    @Query(
        """
            SELECT cards.* FROM cards
            INNER JOIN notes ON cards.note_id = notes.id
            WHERE notes.subject_id = :subjectId
            AND cards.next_review_date <= :now
            ORDER BY cards.next_review_date ASC
        """
    )
    suspend fun getDueCardsBySubject(subjectId: Long, now: Long): List<Card>

    @Query(
        """
            SELECT COUNT(*) FROM cards
            INNER JOIN notes ON cards.note_id = notes.id
            WHERE notes.subject_id = :subjectId
            AND cards.next_review_date <= :now
        """
    )
    fun countDueBySubject(subjectId: Long, now: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM cards WHERE box = :box")
    fun countInBox(box: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM cards WHERE next_review_date <= :now")
    fun countDue(now: Long): Flow<Int>
}