package ph.edu.comteq.cue.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cue_settings")

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val DAILY_GOAL = intPreferencesKey("daily_goal")
        val SORT_BY_TITLE = booleanPreferencesKey("sort_by_title")
    }

    val theme: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.THEME] ?: "system"
    }

    val dailyGoal: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.DAILY_GOAL] ?: 20
    }

    val sortByTitle: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.SORT_BY_TITLE] ?: false
    }

    suspend fun setTheme(value: String) {
        context.dataStore.edit { prefs -> prefs[Keys.THEME] = value }
    }

    suspend fun setDailyGoal(value: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.DAILY_GOAL] = value }
    }

    suspend fun setSortByTitle(value: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.SORT_BY_TITLE] = value }
    }
}