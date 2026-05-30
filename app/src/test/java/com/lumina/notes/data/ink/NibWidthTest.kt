package com.lumina.notes.data.ink

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NibWidthTest {

    @Test fun slowHardPressIsThickest() {
        val w = NibWidth.of(base = 10f, pressure = 1f, distance = 0f)
        assertEquals(10f, w, 0.001f) // full pressure, zero speed -> base
    }

    @Test fun fastStrokeIsThinner() {
        val slow = NibWidth.of(10f, 1f, 0f)
        val fast = NibWidth.of(10f, 1f, 120f)
        assertTrue(fast < slow)
    }

    @Test fun lightPressIsThinner() {
        val hard = NibWidth.of(10f, 1f, 0f)
        val light = NibWidth.of(10f, 0.1f, 0f)
        assertTrue(light < hard)
    }

    @Test fun widthStaysPositive() {
        val w = NibWidth.of(4f, 0f, 1000f)
        assertTrue(w > 0f)
    }

    @Test fun distanceIsEuclidean() {
        assertEquals(5f, NibWidth.distance(0f, 0f, 3f, 4f), 0.001f)
    }
}
