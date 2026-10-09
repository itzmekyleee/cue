package ph.edu.comteq.cue.data.local.dao

import androidx.room3.Insert
import ph.edu.comteq.cue.data.local.entity.Note
import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): Note?

    // Get all notes for a subject
    @Query(
        """
        SELECT * FROM notes
        WHERE subject_id = :subjectId
        AND (title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%')
        AND (:tag = '' OR (',' || tags || ',') LIKE '%,' || :tag || ',%')
        ORDER BY
            CASE WHEN :sortByTitle = 1 THEN title END COLLATE NOCASE ASC,
            CASE WHEN :sortByTitle = 0 THEN updated_at END DESC
        """
    )

    // Get all notes for a subject
    fun getNotes(
        subjectId: Long,
        query: String,
        tag: String,
        sortByTitle: Boolean
    ): Flow<List<Note>>

    // Get all tag strings for a subject
    @Query("SELECT tags FROM notes WHERE subject_id = :subjectId")
    fun getTagStrings(
        subjectId: Long
    ): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM notes WHERE subject_id = :subjectId")
    fun getNoteCount(subjectId: Long): Flow<Int>
    @Query("SELECT COUNT(*) FROM notes WHERE subject_id = :subjectId")
    fun countBySubject(subjectId: Long): Flow<Int>
}