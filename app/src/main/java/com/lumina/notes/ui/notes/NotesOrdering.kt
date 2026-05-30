package com.lumina.notes.ui.notes

import com.lumina.notes.data.local.NoteEntity

/**
 * Pure ordering used by the notes list: pinned notes float to the top, then
 * the selected [NoteSort] orders the remainder. Extracted so it can be unit
 * tested without a database.
 */
object NotesOrdering {
    fun apply(notes: List<NoteEntity>, sort: NoteSort): List<NoteEntity> {
        val comparator: Comparator<NoteEntity> = when (sort) {
            NoteSort.UPDATED -> compareByDescending { it.updatedAt }
            NoteSort.CREATED -> compareByDescending { it.createdAt }
            NoteSort.TITLE -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.title.ifBlank { "￿" } }
        }
        return notes.sortedWith(
            compareByDescending<NoteEntity> { it.isPinned }.then(comparator)
        )
    }
}
