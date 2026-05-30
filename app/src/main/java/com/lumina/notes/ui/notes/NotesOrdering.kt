package com.lumina.notes.ui.notes

import com.lumina.notes.data.local.NoteEntity
import com.lumina.notes.data.local.TagsCodec

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

/** Pure helpers for the tag quick-filter, shared by the ViewModel and tests. */
object TagFiltering {

    /** All distinct tags across [notes], case-folded for dedupe, alpha-sorted. */
    fun collectTags(notes: List<NoteEntity>): List<String> =
        notes.flatMap { TagsCodec.decode(it.tags) }
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }

    /** Keeps only notes carrying [tag] (case-insensitive); null keeps all. */
    fun filterByTag(notes: List<NoteEntity>, tag: String?): List<NoteEntity> {
        if (tag == null) return notes
        return notes.filter { note ->
            TagsCodec.decode(note.tags).any { it.equals(tag, ignoreCase = true) }
        }
    }
}
