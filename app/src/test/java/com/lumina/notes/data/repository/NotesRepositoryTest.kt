package com.lumina.notes.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class NotesRepositoryTest {

    @Test fun duplicateTitleAppendsSuffix() {
        assertEquals("Spesa (copia)", NotesRepository.duplicateTitle("Spesa"))
    }

    @Test fun duplicateTitleKeepsBlankBlank() {
        assertEquals("", NotesRepository.duplicateTitle(""))
        assertEquals("", NotesRepository.duplicateTitle("   "))
    }
}
