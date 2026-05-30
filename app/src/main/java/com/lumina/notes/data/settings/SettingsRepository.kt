package com.lumina.notes.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** UI/behavior preferences backed by DataStore. */
data class AppSettings(
    val dynamicColor: Boolean = true,
    val darkTheme: Boolean? = null, // null = follow system
    val palmRejection: Boolean = true,
)

/** Persisted notes-list view preferences (sort order + filter). */
data class ViewPreferences(
    val sortOrdinal: Int = 0,
    val filterOrdinal: Int = 0,
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val DYNAMIC = booleanPreferencesKey("dynamic_color")
        val DARK = booleanPreferencesKey("dark_theme")
        val DARK_SET = booleanPreferencesKey("dark_theme_set")
        val PALM = booleanPreferencesKey("palm_rejection")
        val SORT = intPreferencesKey("notes_sort")
        val FILTER = intPreferencesKey("notes_filter")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            dynamicColor = p[Keys.DYNAMIC] ?: true,
            darkTheme = if (p[Keys.DARK_SET] == true) (p[Keys.DARK] ?: false) else null,
            palmRejection = p[Keys.PALM] ?: true,
        )
    }

    val viewPreferences: Flow<ViewPreferences> = context.dataStore.data.map { p ->
        ViewPreferences(
            sortOrdinal = p[Keys.SORT] ?: 0,
            filterOrdinal = p[Keys.FILTER] ?: 0,
        )
    }

    suspend fun setSort(ordinal: Int) =
        context.dataStore.edit { it[Keys.SORT] = ordinal }.let {}

    suspend fun setFilter(ordinal: Int) =
        context.dataStore.edit { it[Keys.FILTER] = ordinal }.let {}

    suspend fun setDynamicColor(value: Boolean) =
        context.dataStore.edit { it[Keys.DYNAMIC] = value }.let {}

    suspend fun setDarkTheme(value: Boolean?) {
        context.dataStore.edit {
            if (value == null) {
                it[Keys.DARK_SET] = false
            } else {
                it[Keys.DARK_SET] = true
                it[Keys.DARK] = value
            }
        }
    }

    suspend fun setPalmRejection(value: Boolean) =
        context.dataStore.edit { it[Keys.PALM] = value }.let {}
}
