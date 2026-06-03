package com.lumina.notes.data.ink

import org.junit.Assert.assertEquals
import org.junit.Test

class RecentColorsTest {

    @Test fun newestFirst() {
        val r = RecentColors()
        r.add(0xFF1L); r.add(0xFF2L); r.add(0xFF3L)
        assertEquals(listOf(0xFF3L, 0xFF2L, 0xFF1L), r.colors)
    }

    @Test fun deduplicatesMovingToFront() {
        val r = RecentColors()
        r.add(0xFF1L); r.add(0xFF2L); r.add(0xFF1L)
        assertEquals(listOf(0xFF1L, 0xFF2L), r.colors)
    }

    @Test fun capsAtCapacity() {
        val r = RecentColors(capacity = 3)
        for (c in 1L..5L) r.add(c)
        assertEquals(listOf(5L, 4L, 3L), r.colors)
    }

    @Test fun seedsFromInitial() {
        val r = RecentColors(initial = listOf(0xAL, 0xBL))
        assertEquals(listOf(0xBL, 0xAL), r.colors)
    }
}
