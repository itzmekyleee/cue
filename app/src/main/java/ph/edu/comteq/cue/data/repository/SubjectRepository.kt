package ph.edu.comteq.cue.data.repository

import ph.edu.comteq.cue.data.local.entity.Subject
import kotlinx.coroutines.flow.Flow
import ph.edu.comteq.cue.data.local.dao.SubjectDao

class SubjectRepository(private val dao: SubjectDao) {
    val subjects: Flow<List<Subject>> = dao.getAll()

    suspend fun add(name: String, colorKey: String): Long = dao.insert(Subject(name = name.trim(), colorKey = colorKey))

    suspend fun rename(subject: Subject, newName: String) = dao.update(subject.copy(name = newName.trim()))

    suspend fun delete(subject: Subject) = dao.delete(subject)

    suspend fun getSubject(id: Long): Subject? = dao.getById(id)

    suspend fun clearAll() = dao.deleteAll()
}