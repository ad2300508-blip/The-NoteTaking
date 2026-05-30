package com.lumina.notes.util

import com.lumina.notes.data.local.NoteEntity
import org.junit.Assert.assertTrue
import org.junit.Test

class NotesExporterTest {

    @Test fun emptyLibraryMessage() {
        val md = NotesExporter.toMarkdown(emptyList())
        assertTrue(md.contains("Nessuna nota"))
    }

    @Test fun includesTitlesAndBodies() {
        val notes = listOf(
            NoteEntity(title = "Spesa", body = "latte, pane"),
            NoteEntity(title = "", body = "senza titolo body"),
        )
        val md = NotesExporter.toMarkdown(notes)
        assertTrue(md.contains("## Spesa"))
        assertTrue(md.contains("latte, pane"))
        assertTrue(md.contains("(senza titolo)"))
    }

    @Test fun rendersTagsAndFlags() {
        val notes = listOf(
            NoteEntity(title = "T", tags = "lavoro,idee", isPinned = true, isFavorite = true),
        )
        val md = NotesExporter.toMarkdown(notes)
        assertTrue(md.contains("#lavoro"))
        assertTrue(md.contains("fissata"))
        assertTrue(md.contains("preferita"))
    }

    @Test fun fileNameHasMdExtension() {
        assertTrue(NotesExporter.fileName(0L).endsWith(".md"))
        assertTrue(NotesExporter.fileName(0L).startsWith("lumina-note-"))
    }
}
