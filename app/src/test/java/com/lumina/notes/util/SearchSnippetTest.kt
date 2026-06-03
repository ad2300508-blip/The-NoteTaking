package com.lumina.notes.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchSnippetTest {

    @Test fun emptyQueryReturnsHead() {
        assertEquals("ciao mondo", SearchSnippet.of("ciao mondo", "", radius = 100))
    }

    @Test fun matchIsCentered() {
        val text = "a".repeat(100) + "TARGET" + "b".repeat(100)
        val snip = SearchSnippet.of(text, "target", radius = 10)
        assertTrue(snip.contains("TARGET"))
        assertTrue(snip.startsWith("…"))
        assertTrue(snip.endsWith("…"))
    }

    @Test fun noEllipsisWhenWithinRadius() {
        val snip = SearchSnippet.of("breve testo qui", "testo", radius = 40)
        assertEquals("breve testo qui", snip)
    }

    @Test fun missingMatchFallsBackToHead() {
        val snip = SearchSnippet.of("contenuto senza riscontro", "xyz", radius = 5)
        assertEquals("contenuto ", snip)
    }

    @Test fun newlinesAreFlattened() {
        val snip = SearchSnippet.of("riga uno\nparola\nriga due", "parola", radius = 5)
        assertTrue(!snip.contains("\n"))
    }

    @Test fun matchRangesFindsAllOccurrences() {
        val ranges = SearchSnippet.matchRanges("abcABCabc", "abc")
        assertEquals(3, ranges.size)
        assertEquals(0 until 3, ranges[0])
        assertEquals(3 until 6, ranges[1])
        assertEquals(6 until 9, ranges[2])
    }

    @Test fun matchRangesEmptyWhenNoQuery() {
        assertTrue(SearchSnippet.matchRanges("testo", "").isEmpty())
    }

    @Test fun matchRangesNonOverlapping() {
        val ranges = SearchSnippet.matchRanges("aaaa", "aa")
        assertEquals(2, ranges.size)
    }
}
