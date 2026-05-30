package com.lumina.notes.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class TagsCodecTest {

    @Test fun decodeTrimsAndDropsEmpties() {
        assertEquals(listOf("lavoro", "idee"), TagsCodec.decode(" lavoro , , idee "))
    }

    @Test fun decodeEmptyStringYieldsEmptyList() {
        assertEquals(emptyList<String>(), TagsCodec.decode(""))
    }

    @Test fun encodeDeduplicatesCaseInsensitively() {
        assertEquals("Lavoro,idee", TagsCodec.encode(listOf("Lavoro", "lavoro", "idee", "IDEE")))
    }

    @Test fun encodeStripsCommasAndCollapsesSpaces() {
        assertEquals("a b", TagsCodec.encode(listOf("a,  b")))
    }

    @Test fun addAppendsNewTag() {
        assertEquals("lavoro,idee", TagsCodec.add("lavoro", "idee"))
    }

    @Test fun addIgnoresDuplicate() {
        assertEquals("lavoro", TagsCodec.add("lavoro", "Lavoro"))
    }

    @Test fun removeIsCaseInsensitive() {
        assertEquals("idee", TagsCodec.remove("lavoro,idee", "LAVORO"))
    }

    @Test fun roundTripIsStable() {
        val raw = "casa,lavoro,viaggi"
        assertEquals(raw, TagsCodec.encode(TagsCodec.decode(raw)))
    }
}
