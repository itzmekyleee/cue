package ph.edu.comteq.cue.data.repository

import kotlinx.coroutines.flow.Flow
import ph.edu.comteq.cue.data.local.dao.CardDao
import ph.edu.comteq.cue.data.local.entity.Card

class CardRepository(private val dao: CardDao) {

    fun getCardsForNote(noteId: Long): Flow<List<Card>> = dao.getByNote(noteId)

    suspend fun getCard(id: Long): Card? = dao.getById(id)

    suspend fun add(noteId: Long, question: String, answer: String): Long =
        dao.insert(Card(noteId = noteId, question = question.trim(), answer = answer.trim()))

    suspend fun update(card: Card) = dao.update(card)

    suspend fun delete(card: Card) = dao.delete(card)

    suspend fun getDueCards(now: Long = System.currentTimeMillis()): List<Card> =
        dao.getDueCards(now)

    suspend fun getDueCardsForSubject(
        subjectId: Long,
        now: Long = System.currentTimeMillis()
    ): List<Card> = dao.getDueCardsBySubject(subjectId, now)

    fun dueCount(subjectId: Long, now: Long = System.currentTimeMillis()): Flow<Int> =
        dao.countDueBySubject(subjectId, now)

    fun totalDueCount(now: Long = System.currentTimeMillis()): Flow<Int> = dao.countDue(now)

    fun countInBox(box: Int): Flow<Int> = dao.countInBox(box)
}