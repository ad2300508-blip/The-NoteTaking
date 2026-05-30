package com.lumina.notes.ui.notes

import com.lumina.notes.data.local.NoteEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class NotesOrderingTest {

    private fun note(
        id: String,
        title: String = "",
        pinned: Boolean = false,
        updated: Long = 0,
        created: Long = 0,
    ) = NoteEntity(
        id = id, title = title, isPinned = pinned,
        updatedAt = updated, createdAt = created,
    )

    @Test fun pinnedAlwaysFirst() {
        val notes = listOf(
            note("a", updated = 100),
            note("b", pinned = true, updated = 1),
            note("c", updated = 50),
        )
        val ids = NotesOrdering.apply(notes, NoteSort.UPDATED).map { it.id }
        assertEquals(listOf("b", "a", "c"), ids)
    }

    @Test fun updatedSortIsDescending() {
        val notes = listOf(
            note("old", updated = 10),
            note("new", updated = 99),
            note("mid", updated = 50),
        )
        val ids = NotesOrdering.apply(notes, NoteSort.UPDATED).map { it.id }
        assertEquals(listOf("new", "mid", "old"), ids)
    }

    @Test fun createdSortUsesCreationTime() {
        val notes = listOf(
            note("a", updated = 100, created = 1),
            note("b", updated = 1, created = 100),
        )
        val ids = NotesOrdering.apply(notes, NoteSort.CREATED).map { it.id }
        assertEquals(listOf("b", "a"), ids)
    }

    @Test fun titleSortIsCaseInsensitiveAlphabetical() {
        val notes = listOf(
            note("z", title = "zebra"),
            note("a", title = "Apple"),
            note("m", title = "mango"),
        )
        val ids = NotesOrdering.apply(notes, NoteSort.TITLE).map { it.id }
        assertEquals(listOf("a", "m", "z"), ids)
    }

    @Test fun untitledNotesSortLast() {
        val notes = listOf(
            note("blank", title = ""),
            note("named", title = "Alpha"),
        )
        val ids = NotesOrdering.apply(notes, NoteSort.TITLE).map { it.id }
        assertEquals(listOf("named", "blank"), ids)
    }
}
