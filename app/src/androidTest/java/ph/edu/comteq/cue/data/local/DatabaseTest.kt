package ph.edu.comteq.cue.data.local

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ph.edu.comteq.cue.data.local.entity.Card
import ph.edu.comteq.cue.data.local.entity.Note
import ph.edu.comteq.cue.data.local.entity.ReviewLog
import ph.edu.comteq.cue.data.local.entity.Subject

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder<AppDatabase>(context)
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    // Small helpers so each test stays short
    private suspend fun addSubject(name: String = "Biology"): Long =
        db.subjectDao().insert(Subject(name = name, colorKey = "teal"))

    private suspend fun addNote(
        subjectId: Long,
        title: String,
        body: String = "",
        tags: String = ""
    ): Long = db.noteDao().insert(
        Note(subjectId = subjectId, title = title, body = body, tags = tags)
    )

    @Test
    fun insertSubject_thenReadItBack() = runTest {
        addSubject("Biology")

        val subjects = db.subjectDao().getAll().first()

        assertEquals(1, subjects.size)
        assertEquals("Biology", subjects[0].name)
    }

    @Test
    fun deleteSubject_alsoDeletesItsNotes() = runTest {
        val subjectId = addSubject()
        addNote(subjectId, "Photosynthesis")
        val subject = db.subjectDao().getById(subjectId)!!

        db.subjectDao().delete(subject)

        val notes = db.noteDao().getNotes(subjectId, "", "", false).first()
        assertEquals(0, notes.size)
    }

    @Test
    fun search_findsTextInTitleOrBody() = runTest {
        val subjectId = addSubject()
        addNote(subjectId, "Photosynthesis", body = "Plants use sunlight")
        addNote(subjectId, "Cell structure", body = "Mitochondria")

        val byTitle = db.noteDao().getNotes(subjectId, "photo", "", false).first()
        val byBody = db.noteDao().getNotes(subjectId, "mitochondria", "", false).first()

        assertEquals(1, byTitle.size)
        assertEquals(1, byBody.size)
        assertEquals("Cell structure", byBody[0].title)
    }

    @Test
    fun tagFilter_matchesWholeTagOnly() = runTest {
        val subjectId = addSubject()
        addNote(subjectId, "Cells note", tags = "biology,cells")

        val exact = db.noteDao().getNotes(subjectId, "", "cells", false).first()
        val partial = db.noteDao().getNotes(subjectId, "", "cell", false).first()

        assertEquals(1, exact.size)
        assertEquals(0, partial.size)
    }

    @Test
    fun sortByTitle_ordersAToZ_ignoringCapitals() = runTest {
        val subjectId = addSubject()
        addNote(subjectId, "Banana")
        addNote(subjectId, "apple")
        addNote(subjectId, "Cherry")

        val titles = db.noteDao().getNotes(subjectId, "", "", true).first().map { it.title }

        assertEquals(listOf("apple", "Banana", "Cherry"), titles)
    }

    @Test
    fun getDueCards_onlyReturnsCardsDueByNow() = runTest {
        val subjectId = addSubject()
        val noteId = addNote(subjectId, "Photosynthesis")
        db.cardDao().insert(Card(noteId = noteId, question = "Due", answer = "A", nextReviewDate = 1_000))
        db.cardDao().insert(Card(noteId = noteId, question = "Later", answer = "B", nextReviewDate = 9_000))

        val due = db.cardDao().getDueCards(now = 5_000)

        assertEquals(1, due.size)
        assertEquals("Due", due[0].question)
    }

    @Test
    fun reviewLog_countsTotalAndCorrect() = runTest {
        val subjectId = addSubject()
        val noteId = addNote(subjectId, "Photosynthesis")
        val cardId = db.cardDao().insert(Card(noteId = noteId, question = "Q", answer = "A"))

        db.reviewLogDao().insert(ReviewLog(cardId = cardId, wasCorrect = true))
        db.reviewLogDao().insert(ReviewLog(cardId = cardId, wasCorrect = true))
        db.reviewLogDao().insert(ReviewLog(cardId = cardId, wasCorrect = false))

        assertEquals(3, db.reviewLogDao().countAll().first())
        assertEquals(2, db.reviewLogDao().countCorrect().first())
    }
}