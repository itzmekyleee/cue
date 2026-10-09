package ph.edu.comteq.cue.ui.subjects

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import ph.edu.comteq.cue.data.local.entity.Subject
import ph.edu.comteq.cue.data.repository.CardRepository
import ph.edu.comteq.cue.data.repository.SubjectRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlin.time.Duration.Companion.seconds

class SubjectsViewModel(
    private val repository: SubjectRepository,
    cardRepository: CardRepository
) : ViewModel() {
    val totalDueCount: StateFlow<Int> = cardRepository.totalDueCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )
    private val pendingDeleteIds = MutableStateFlow<Set<Long>>(emptySet())
    private val allSubjects: StateFlow<List<Subject>> = repository.subjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val subjects: StateFlow<List<Subject>> = combine(
        allSubjects,
        pendingDeleteIds
    ) { subjects, hiddenIds ->
        subjects.filterNot { it.id in hiddenIds }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun addSubject(name: String, colorKey: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.add(name, colorKey)
        }
    }

    fun renameSubject(subject: Subject, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.rename(subject, newName)
        }
    }

    fun deleteSubject(subject: Subject) {
        pendingDeleteIds.value += subject.id
        viewModelScope.launch {
            delay(5.seconds)
            if (subject.id in pendingDeleteIds.value) {
                repository.delete(subject)
                pendingDeleteIds.value -= subject.id
            }
        }
    }

    fun undoSubjectDelete(subject: Subject) {
        pendingDeleteIds.value -= subject.id
    }
}