package com.lumina.notes.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ColorHsvTest {

    @Test fun redHue() {
        assertEquals(0xFFFF0000L, ColorHsv.toArgb(0f, 1f, 1f))
    }

    @Test fun greenHue() {
        assertEquals(0xFF00FF00L, ColorHsv.toArgb(120f, 1f, 1f))
    }

    @Test fun blueHue() {
        assertEquals(0xFF0000FFL, ColorHsv.toArgb(240f, 1f, 1f))
    }

    @Test fun whiteIsZeroSaturationFullValue() {
        assertEquals(0xFFFFFFFFL, ColorHsv.toArgb(0f, 0f, 1f))
    }

    @Test fun blackIsZeroValue() {
        assertEquals(0xFF000000L, ColorHsv.toArgb(123f, 1f, 0f))
    }

    @Test fun hueWrapsAround() {
        assertEquals(ColorHsv.toArgb(0f, 1f, 1f), ColorHsv.toArgb(360f, 1f, 1f))
    }

    @Test fun alphaIsAlwaysOpaque() {
        val argb = ColorHsv.toArgb(200f, 0.5f, 0.5f)
        assertEquals(0xFFL, (argb ushr 24) and 0xFF)
    }
}
