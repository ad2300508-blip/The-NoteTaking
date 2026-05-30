package com.lumina.notes.ui.notes

import com.lumina.notes.data.local.NoteEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class TagFilteringTest {

    private fun note(id: String, tags: String) =
        NoteEntity(id = id, tags = tags)

    @Test fun collectTagsIsDistinctCaseFoldedAndSorted() {
        val notes = listOf(
            note("a", "Lavoro,idee"),
            note("b", "lavoro,Casa"),
            note("c", ""),
        )
        assertEquals(listOf("casa", "idee", "lavoro"), TagFiltering.collectTags(notes).map { it.lowercase() })
    }

    @Test fun collectTagsKeepsFirstSpelling() {
        val notes = listOf(note("a", "Lavoro"), note("b", "lavoro"))
        assertEquals(listOf("Lavoro"), TagFiltering.collectTags(notes))
    }

    @Test fun filterByNullKeepsEverything() {
        val notes = listOf(note("a", "x"), note("b", ""))
        assertEquals(2, TagFiltering.filterByTag(notes, null).size)
    }

    @Test fun filterByTagIsCaseInsensitive() {
        val notes = listOf(
            note("a", "Lavoro,idee"),
            note("b", "casa"),
        )
        assertEquals(listOf("a"), TagFiltering.filterByTag(notes, "LAVORO").map { it.id })
    }

    @Test fun filterByMissingTagYieldsEmpty() {
        val notes = listOf(note("a", "x"), note("b", "y"))
        assertEquals(emptyList<String>(), TagFiltering.filterByTag(notes, "z").map { it.id })
    }
}
