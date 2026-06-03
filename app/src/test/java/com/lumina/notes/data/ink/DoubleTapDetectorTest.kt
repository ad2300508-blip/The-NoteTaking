package com.lumina.notes.data.ink

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DoubleTapDetectorTest {

    @Test fun singleTapIsNotDouble() {
        val d = DoubleTapDetector()
        assertFalse(d.onTap(10f, 10f, now = 1000))
    }

    @Test fun twoQuickCloseTapsAreDouble() {
        val d = DoubleTapDetector(timeoutMs = 350, slopPx = 48f)
        assertFalse(d.onTap(10f, 10f, now = 1000))
        assertTrue(d.onTap(12f, 14f, now = 1200))
    }

    @Test fun tooSlowIsNotDouble() {
        val d = DoubleTapDetector(timeoutMs = 350)
        assertFalse(d.onTap(10f, 10f, now = 1000))
        assertFalse(d.onTap(10f, 10f, now = 2000))
    }

    @Test fun tooFarIsNotDouble() {
        val d = DoubleTapDetector(slopPx = 48f)
        assertFalse(d.onTap(10f, 10f, now = 1000))
        assertFalse(d.onTap(500f, 500f, now = 1100))
    }

    @Test fun tripleTapIsOneDoubleNotTwo() {
        val d = DoubleTapDetector()
        assertFalse(d.onTap(10f, 10f, now = 1000)) // tap 1
        assertTrue(d.onTap(10f, 10f, now = 1100))  // tap 2 -> double, resets
        assertFalse(d.onTap(10f, 10f, now = 1200)) // tap 3 -> fresh single
    }

    @Test fun resetClearsState() {
        val d = DoubleTapDetector()
        d.onTap(10f, 10f, now = 1000)
        d.reset()
        assertFalse(d.onTap(10f, 10f, now = 1100))
    }
}
