package com.lumina.notes.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.notes.data.local.NoteEntity
import com.lumina.notes.data.repository.NotesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NoteFilter { ALL, FAVORITES, PINNED }

data class NotesUiState(
    val notes: List<NoteEntity> = emptyList(),
    val query: String = "",
    val filter: NoteFilter = NoteFilter.ALL,
    val loading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
class NotesListViewModel(private val repo: NotesRepository) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(NoteFilter.ALL)

    val state: StateFlow<NotesUiState> =
        kotlinx.coroutines.flow.combine(
            query.flatMapLatest { q ->
                if (q.isBlank()) repo.observeNotes() else repo.search(q)
            },
            query,
            filter,
        ) { notes, q, f ->
            val filtered = when (f) {
                NoteFilter.ALL -> notes
                NoteFilter.FAVORITES -> notes.filter { it.isFavorite }
                NoteFilter.PINNED -> notes.filter { it.isPinned }
            }
            NotesUiState(notes = filtered, query = q, filter = f, loading = false)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotesUiState())

    fun onQueryChange(value: String) { query.value = value }
    fun onFilterChange(value: NoteFilter) { filter.value = value }

    /** Creates an empty note and returns its id so the UI can open it. */
    fun createNote(onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val note = NoteEntity(colorSeed = (0..6).random())
            repo.save(note)
            onCreated(note.id)
        }
    }

    fun togglePin(note: NoteEntity) = viewModelScope.launch { repo.togglePin(note) }
    fun toggleFavorite(note: NoteEntity) = viewModelScope.launch { repo.toggleFavorite(note) }
    fun delete(note: NoteEntity) = viewModelScope.launch { repo.delete(note) }
}
