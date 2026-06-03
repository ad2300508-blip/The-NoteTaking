package com.lumina.notes.util

import com.lumina.notes.data.local.NoteEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryStatsTest {

    private fun note(
        ink: String = "",
        fav: Boolean = false,
        pin: Boolean = false,
        tags: String = "",
    ) = NoteEntity(inkJson = ink, isFavorite = fav, isPinned = pin, tags = tags)

    @Test fun emptyLibrary() {
        val s = LibraryStatsCalculator.of(emptyList())
        assertEquals(0, s.total)
        assertEquals("0 note", s.summary())
    }

    @Test fun countsInkFavoritesPinned() {
        val notes = listOf(
            note(ink = "[{\"p\":[0,0,1,1,1,1]}]", fav = true),
            note(pin = true),
            note(),
        )
        val s = LibraryStatsCalculator.of(notes)
        assertEquals(3, s.total)
        assertEquals(1, s.withInk)
        assertEquals(1, s.favorites)
        assertEquals(1, s.pinned)
    }

    @Test fun distinctTagsAreCaseFolded() {
        val notes = listOf(note(tags = "Lavoro,idee"), note(tags = "lavoro,casa"))
        assertEquals(3, LibraryStatsCalculator.of(notes).distinctTags)
    }

    @Test fun summaryMentionsInkAndTags() {
        val notes = listOf(
            note(ink = "[{\"p\":[0,0,1,2,2,1]}]", tags = "a"),
            note(tags = "b"),
        )
        assertEquals("2 note · 1 con disegni · 2 tag", LibraryStatsCalculator.of(notes).summary())
    }

    @Test fun singularNote() {
        assertEquals("1 nota", LibraryStatsCalculator.of(listOf(note())).summary())
    }
}
