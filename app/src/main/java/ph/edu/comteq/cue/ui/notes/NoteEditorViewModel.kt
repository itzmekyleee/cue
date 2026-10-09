package ph.edu.comteq.cue.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ph.edu.comteq.cue.data.local.entity.Card
import ph.edu.comteq.cue.data.local.entity.Note
import ph.edu.comteq.cue.data.repository.CardRepository
import ph.edu.comteq.cue.data.repository.NoteRepository

class NoteEditorViewModel(
    private val subjectId: Long,
    initialNoteId: Long?,
    private val noteRepository: NoteRepository,
    private val cardRepository: CardRepository
) : ViewModel() {
    private val noteId = MutableStateFlow(initialNoteId)

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val _body = MutableStateFlow("")
    val body: StateFlow<String> = _body

    private val _tagsText = MutableStateFlow("")
    val tagsText: StateFlow<String> = _tagsText

    private val _hasUnsavedChanges = MutableStateFlow(false)
    val hasUnsavedChanges: StateFlow<Boolean> = _hasUnsavedChanges

    val cards: StateFlow<List<Card>> = noteId
        .flatMapLatest { noteId ->
            noteId?.let { cardRepository.getCardsForNote(it) } ?: flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        val id = initialNoteId
        if (id != null) {
            viewModelScope.launch {
                noteRepository.getNote(id)?.let { note ->
                    _title.value = note.title
                    _body.value = note.body
                    _tagsText.value = note.tags
                }
            }
        }
    }

    fun onTitleChange(value: String) {
        _title.value = value
        _hasUnsavedChanges.value = true
    }

    fun onBodyChange(value: String) {
        _body.value = value
        _hasUnsavedChanges.value = true
    }

    fun onTagsTextChange(value: String) {
        _tagsText.value = value
        _hasUnsavedChanges.value = true
    }

    fun save() {
        viewModelScope.launch {
            val saveId = noteRepository.save(

                Note(
                    id = noteId.value ?: 0,
                    subjectId = subjectId,
                    title = title.value,
                    body = body.value,
                    tags = tagsText.value
                )
            )
            noteId.value = saveId
            _hasUnsavedChanges.value = false
        }
    }

    fun addCard(question: String, answer: String) {
        if (question.isBlank() || answer.isBlank()) return
        viewModelScope.launch {
            val id = noteId.value ?: noteRepository.save(
                Note(
                    subjectId = subjectId,
                    title = title.value,
                    body = body.value,
                    tags = tagsText.value
                )
            ).also { noteId.value = it }
            cardRepository.add(id, question, answer)
        }
    }

    fun updateCard(card: Card) {
        viewModelScope.launch {
            cardRepository.update(card)
        }
    }

    fun deleteCard(card: Card) {
        viewModelScope.launch {
            cardRepository.delete(card)
        }
    }
}