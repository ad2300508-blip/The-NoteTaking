package com.lumina.notes.ui.editor

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumina.notes.data.ink.InkSerializer
import com.lumina.notes.data.local.NoteEntity
import com.lumina.notes.data.local.TagsCodec
import com.lumina.notes.data.repository.NotesRepository
import com.lumina.notes.ui.ink.InkController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

class EditorViewModel(
    private val repo: NotesRepository,
    private val noteId: String,
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _body = MutableStateFlow("")
    val body: StateFlow<String> = _body.asStateFlow()

    private val _colorSeed = MutableStateFlow(0)
    val colorSeed: StateFlow<Int> = _colorSeed.asStateFlow()

    private val _paperStyle = MutableStateFlow(0)
    val paperStyle: StateFlow<Int> = _paperStyle.asStateFlow()

    private val _isPinned = MutableStateFlow(false)
    val isPinned: StateFlow<Boolean> = _isPinned.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags.asStateFlow()

    /** 0 = text, 1 = ink. Restored from the note; persisted on change. */
    private val _lastMode = MutableStateFlow(0)
    val lastMode: StateFlow<Int> = _lastMode.asStateFlow()

    val ink = InkController()

    private var loaded: NoteEntity? = null

    init {
        viewModelScope.launch {
            val note = repo.getNote(noteId)
            if (note != null) {
                loaded = note
                _title.value = note.title
                _body.value = note.body
                _colorSeed.value = note.colorSeed
                _paperStyle.value = note.paperStyle
                _isPinned.value = note.isPinned
                _isFavorite.value = note.isFavorite
                _tags.value = TagsCodec.decode(note.tags)
                _lastMode.value = note.lastMode
                InkSerializer.decode(note.inkJson).let { strokes ->
                    ink.strokes.clear()
                    ink.strokes.addAll(strokes)
                }
            }
            startAutosave()
        }
    }

    private var autosaveStarted = false
    private fun startAutosave() {
        if (autosaveStarted) return
        autosaveStarted = true
        // Coalesce rapid edits and persist quietly in the background.
        viewModelScope.launch {
            snapshotFlow { ink.revision }
                .drop(1)
                .debounce(600)
                .collect { persist() }
        }
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(_title, _body) { t, b -> t to b }
                .drop(1)
                .debounce(600)
                .collect { persist() }
        }
    }

    fun onTitleChange(value: String) { _title.value = value }
    fun onBodyChange(value: String) { _body.value = value }
    fun setColorSeed(seed: Int) { _colorSeed.value = seed; viewModelScope.launch { persist() } }
    fun setPaperStyle(style: Int) { _paperStyle.value = style; viewModelScope.launch { persist() } }

    fun togglePin() { _isPinned.value = !_isPinned.value; viewModelScope.launch { persist() } }
    fun toggleFavorite() { _isFavorite.value = !_isFavorite.value; viewModelScope.launch { persist() } }

    fun addTag(tag: String) {
        val updated = TagsCodec.decode(TagsCodec.add(TagsCodec.encode(_tags.value), tag))
        _tags.value = updated
        viewModelScope.launch { persist() }
    }

    fun removeTag(tag: String) {
        _tags.value = _tags.value.filterNot { it.equals(tag, ignoreCase = true) }
        viewModelScope.launch { persist() }
    }

    fun setLastMode(mode: Int) {
        if (_lastMode.value == mode) return
        _lastMode.value = mode
        viewModelScope.launch { persist() }
    }

    suspend fun persist() {
        val base = loaded ?: NoteEntity(id = noteId)
        val updated = base.copy(
            title = _title.value,
            body = _body.value,
            inkJson = ink.encode(),
            colorSeed = _colorSeed.value,
            paperStyle = _paperStyle.value,
            tags = TagsCodec.encode(_tags.value),
            lastMode = _lastMode.value,
            isPinned = _isPinned.value,
            isFavorite = _isFavorite.value,
        )
        loaded = updated
        repo.save(updated)
    }

    /** Explicit delete from the editor's trash action. */
    fun deleteNote() {
        viewModelScope.launch { repo.deleteById(noteId) }
    }

    /** Deletes the note if it has no content (so abandoned blank notes vanish). */
    fun discardIfEmpty() {
        viewModelScope.launch {
            if (_title.value.isBlank() && _body.value.isBlank() && ink.strokes.isEmpty()) {
                repo.deleteById(noteId)
            } else {
                persist()
            }
        }
    }
}
