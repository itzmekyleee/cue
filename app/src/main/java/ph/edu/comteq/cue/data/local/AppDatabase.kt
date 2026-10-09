package ph.edu.comteq.cue.data.local

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import ph.edu.comteq.cue.data.local.dao.CardDao
import ph.edu.comteq.cue.data.local.dao.NoteDao
import ph.edu.comteq.cue.data.local.dao.ReviewLogDao
import ph.edu.comteq.cue.data.local.dao.SubjectDao
import ph.edu.comteq.cue.data.local.entity.Card
import ph.edu.comteq.cue.data.local.entity.Note
import ph.edu.comteq.cue.data.local.entity.ReviewLog
import ph.edu.comteq.cue.data.local.entity.Subject


@Database(
    entities = [Subject::class, Note::class, Card::class, ReviewLog::class],
    version = 1,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun noteDao(): NoteDao
    abstract fun cardDao(): CardDao
    abstract fun reviewLogDao(): ReviewLogDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                val value = instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cue.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
                value
            }
        }
    }
}
