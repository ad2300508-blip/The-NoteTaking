package com.lumina.notes.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.notes.data.repository.NotesRepository
import com.lumina.notes.data.settings.AppSettings
import com.lumina.notes.data.settings.SettingsRepository
import com.lumina.notes.util.LibraryStats
import com.lumina.notes.util.LibraryStatsCalculator
import com.lumina.notes.util.NotesExporter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repo: SettingsRepository,
    private val notesRepo: NotesRepository,
) : ViewModel() {

    val state: StateFlow<AppSettings> =
        repo.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    val libraryStats: StateFlow<LibraryStats> =
        notesRepo.observeNotes()
            .map { LibraryStatsCalculator.of(it) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                LibraryStats(0, 0, 0, 0, 0),
            )

    fun setDynamicColor(value: Boolean) = viewModelScope.launch { repo.setDynamicColor(value) }
    fun setDarkTheme(value: Boolean?) = viewModelScope.launch { repo.setDarkTheme(value) }
    fun setPalmRejection(value: Boolean) = viewModelScope.launch { repo.setPalmRejection(value) }
    fun setFontScale(value: Float) = viewModelScope.launch { repo.setFontScale(value) }
    fun setPressureSensitivity(value: Float) =
        viewModelScope.launch { repo.setPressureSensitivity(value) }

    /** Builds a Markdown export of all notes and hands it to [onReady]. */
    fun exportAll(onReady: (fileName: String, content: String) -> Unit) {
        viewModelScope.launch {
            val notes = notesRepo.observeNotes().first()
            onReady(NotesExporter.fileName(), NotesExporter.toMarkdown(notes))
        }
    }
}
