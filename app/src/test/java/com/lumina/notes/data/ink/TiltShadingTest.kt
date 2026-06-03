package com.lumina.notes.data.ink

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TiltShadingTest {

    @Test fun uprightPenIsNeutral() {
        assertEquals(1f, TiltShading.widthMultiplier(0f), 0.001f)
    }

    @Test fun invalidTiltIsNeutral() {
        assertEquals(1f, TiltShading.widthMultiplier(Float.NaN), 0.001f)
        assertEquals(1f, TiltShading.widthMultiplier(-0.5f), 0.001f)
    }

    @Test fun moreTiltMeansWider() {
        val small = TiltShading.widthMultiplier(0.2f)
        val large = TiltShading.widthMultiplier(0.9f)
        assertTrue(large > small)
    }

    @Test fun clampedAtMaxBroaden() {
        val w = TiltShading.widthMultiplier(3f, maxBroaden = 2.5f)
        assertEquals(2.5f, w, 0.001f)
    }

    @Test fun respectsCustomMax() {
        val w = TiltShading.widthMultiplier(3f, maxBroaden = 4f)
        assertEquals(4f, w, 0.001f)
    }
}
