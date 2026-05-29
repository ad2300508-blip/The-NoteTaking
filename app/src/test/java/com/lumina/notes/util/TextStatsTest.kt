package com.lumina.notes.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TextStatsTest {

    @Test fun emptyTextHasNoWords() {
        val s = TextStatsCalculator.of("   ")
        assertEquals(0, s.words)
        assertEquals(0, s.readingMinutes)
    }

    @Test fun countsWordsIgnoringExtraWhitespace() {
        val s = TextStatsCalculator.of("  ciao   mondo\nnuova riga ")
        assertEquals(4, s.words)
    }

    @Test fun charactersIncludeRawLength() {
        assertEquals(5, TextStatsCalculator.of("ab de").characters)
    }

    @Test fun readingTimeRoundsUpToAtLeastOneMinute() {
        val s = TextStatsCalculator.of("una manciata di parole")
        assertEquals(1, s.readingMinutes)
    }

    @Test fun readingTimeScalesWithLength() {
        val text = (1..500).joinToString(" ") { "parola" }
        val s = TextStatsCalculator.of(text)
        assertEquals(500, s.words)
        assertEquals(3, s.readingMinutes) // ceil(500/200) = 3
    }

    @Test fun summaryUsesSingularForOneWord() {
        assertEquals("1 parola · 1 min", TextStatsCalculator.of("ciao").summary())
    }
}
