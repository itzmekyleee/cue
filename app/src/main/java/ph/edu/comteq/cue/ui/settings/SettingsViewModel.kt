package ph.edu.comteq.cue.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ph.edu.comteq.cue.data.datastore.SettingsDataStore
import ph.edu.comteq.cue.data.repository.SubjectRepository

class SettingsViewModel(
    private val dataStore: SettingsDataStore,
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    val theme: StateFlow<String> = dataStore.theme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "system")

    val dailyGoal: StateFlow<Int> = dataStore.dailyGoal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 20)

    val sortByTitle: StateFlow<Boolean> = dataStore.sortByTitle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setTheme(value: String) {
        viewModelScope.launch { dataStore.setTheme(value) }
    }

    fun setDailyGoal(value: Int) {
        viewModelScope.launch { dataStore.setDailyGoal(value) }
    }

    fun setSortByTitle(value: Boolean) {
        viewModelScope.launch { dataStore.setSortByTitle(value) }
    }

    fun clearAllData() {
        viewModelScope.launch { subjectRepository.clearAll() }
    }
}