package ph.edu.comteq.cue.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ph.edu.comteq.cue.data.local.dao.NoteDao
import ph.edu.comteq.cue.data.local.entity.Note


class NoteRepository(private val dao: NoteDao) {
    fun getNotes(
        subjectId: Long,
        query: String = "",
        tag: String = "",
        sortByTitle: Boolean = false
    ): Flow<List<Note>> = dao.getNotes(subjectId, query.trim(), tag, sortByTitle)

    fun getTags(subjectId: Long): Flow<List<String>> = dao.getTagStrings(subjectId).map { rows ->
        rows.flatMap { it.split(".") }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }

    fun noteCount(subjectId: Long): Flow<Int> = dao.getNoteCount(subjectId)

    suspend fun getNote(id: Long): Note? = dao.getById(id)

    suspend fun save(note: Note): Long {
        val clean = note.copy(tags = cleanTags(note.tags))
        return if (clean.id == 0L) {
            dao.insert(clean)
        } else {
            dao.update(clean.copy(updatedAt = System.currentTimeMillis()))
            clean.id
        }
    }

    suspend fun delete(note: Note) = dao.delete(note)

    private fun cleanTags(raw: String): String =
        raw.split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .distinct()
            .joinToString(",")
}