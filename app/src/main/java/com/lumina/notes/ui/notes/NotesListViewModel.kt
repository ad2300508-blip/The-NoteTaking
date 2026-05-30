package com.lumina.notes.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.notes.data.local.NoteEntity
import com.lumina.notes.data.repository.NotesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NoteFilter { ALL, FAVORITES, PINNED }

enum class NoteSort {
    UPDATED, CREATED, TITLE;

    val label: String
        get() = when (this) {
            UPDATED -> "Ultima modifica"
            CREATED -> "Data creazione"
            TITLE -> "Titolo (A→Z)"
        }
}

data class NotesUiState(
    val notes: List<NoteEntity> = emptyList(),
    val query: String = "",
    val filter: NoteFilter = NoteFilter.ALL,
    val sort: NoteSort = NoteSort.UPDATED,
    val activeTag: String? = null,
    val allTags: List<String> = emptyList(),
    val loading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
class NotesListViewModel(private val repo: NotesRepository) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(NoteFilter.ALL)
    private val sort = MutableStateFlow(NoteSort.UPDATED)
    private val activeTag = MutableStateFlow<String?>(null)

    /** A note awaiting a possible undo after a swipe-delete. */
    private var pendingDelete: NoteEntity? = null

    val state: StateFlow<NotesUiState> =
        combine(
            query.flatMapLatest { q ->
                if (q.isBlank()) repo.observeNotes() else repo.search(q)
            },
            query,
            filter,
            sort,
            activeTag,
        ) { notes, q, f, s, tag ->
            // The set of tags offered as quick filters, across all notes.
            val allTags = TagFiltering.collectTags(notes)

            val byFilter = when (f) {
                NoteFilter.ALL -> notes
                NoteFilter.FAVORITES -> notes.filter { it.isFavorite }
                NoteFilter.PINNED -> notes.filter { it.isPinned }
            }
            val filtered = TagFiltering.filterByTag(byFilter, tag)

            // Pinned always float to the top; the chosen sort orders the rest.
            val ordered = NotesOrdering.apply(filtered, s)
            NotesUiState(
                notes = ordered, query = q, filter = f, sort = s,
                activeTag = tag, allTags = allTags, loading = false,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotesUiState())

    fun onQueryChange(value: String) { query.value = value }
    fun onFilterChange(value: NoteFilter) { filter.value = value }
    fun onSortChange(value: NoteSort) { sort.value = value }

    /** Toggles a tag filter; selecting the active tag again clears it. */
    fun onTagClick(tag: String) {
        activeTag.value = if (activeTag.value.equals(tag, ignoreCase = true)) null else tag
    }
    fun clearTagFilter() { activeTag.value = null }

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

    /** Removes [note] but keeps a copy so [undoDelete] can restore it verbatim. */
    fun deleteWithUndo(note: NoteEntity) {
        pendingDelete = note
        viewModelScope.launch { repo.delete(note) }
    }

    fun undoDelete() {
        val note = pendingDelete ?: return
        pendingDelete = null
        viewModelScope.launch { repo.restore(note) }
    }
}
