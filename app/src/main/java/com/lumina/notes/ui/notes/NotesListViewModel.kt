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

data class NotesUiState(
    val notes: List<NoteEntity> = emptyList(),
    val query: String = "",
    val loading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
class NotesListViewModel(private val repo: NotesRepository) : ViewModel() {

    private val query = MutableStateFlow("")

    val state: StateFlow<NotesUiState> =
        query
            .flatMapLatest { q ->
                if (q.isBlank()) repo.observeNotes() else repo.search(q)
            }
            .let { notesFlow ->
                kotlinx.coroutines.flow.combine(notesFlow, query) { notes, q ->
                    NotesUiState(notes = notes, query = q, loading = false)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotesUiState())

    fun onQueryChange(value: String) { query.value = value }

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
