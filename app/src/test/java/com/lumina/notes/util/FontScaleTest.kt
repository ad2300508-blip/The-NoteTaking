package com.lumina.notes.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FontScaleTest {

    @Test fun clampsBelowMin() {
        assertEquals(FontScale.MIN, FontScale.sanitize(0.5f), 0.0001f)
    }

    @Test fun clampsAboveMax() {
        assertEquals(FontScale.MAX, FontScale.sanitize(3f), 0.0001f)
    }

    @Test fun keepsValidValue() {
        assertEquals(1.2f, FontScale.sanitize(1.2f), 0.0001f)
    }

    @Test fun invalidFallsBackToDefault() {
        assertEquals(FontScale.DEFAULT, FontScale.sanitize(Float.NaN), 0.0001f)
        assertEquals(FontScale.DEFAULT, FontScale.sanitize(0f), 0.0001f)
    }

    @Test fun labelIsPercent() {
        assertEquals("100%", FontScale.label(1.0f))
        assertEquals("120%", FontScale.label(1.2f))
    }
}
