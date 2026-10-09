package ph.edu.comteq.cue.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ph.edu.comteq.cue.data.datastore.SettingsDataStore
import ph.edu.comteq.cue.data.local.entity.Note
import ph.edu.comteq.cue.data.repository.CardRepository
import ph.edu.comteq.cue.data.repository.NoteRepository

class NotesListViewModel(
    private val subjectId: Long,
    private val repository: NoteRepository,
    cardRepository: CardRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {
    private val searchText = MutableStateFlow("")
    private val selectedTag = MutableStateFlow("")
    private val sortByTitle = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            sortByTitle.value = settingsDataStore.sortByTitle.first()
        }
    }

    val notes: StateFlow<List<Note>> = combine(
        searchText, selectedTag, sortByTitle
    ) { query, tag, byTitle -> Triple(query, tag, byTitle) }
        .flatMapLatest { (query, tag, byTitle) ->
            repository.getNotes(subjectId, query, tag, byTitle)
        }

        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val tags: StateFlow<List<String>> = repository.getTags(subjectId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val dueCount: StateFlow<Int> = cardRepository.dueCount(subjectId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    fun onSearchTextChange(text: String) {
        searchText.value = text
    }

    fun onSelectedTagChange(tag: String) {
        selectedTag.value = tag
    }

    fun onSortByTitle() {
        sortByTitle.value = !sortByTitle.value
    }

    fun onTagSelected(tag: String) {
        selectedTag.value = tag
    }

    fun onSortByTitle(byTitle: Boolean) {
        sortByTitle.value = byTitle
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.delete(note)
        }
    }
}