package com.lumina.notes.data.ink

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScribbleDetectorTest {

    private fun pts(vararg xy: Pair<Float, Float>) =
        xy.map { StrokePoint(it.first, it.second) }

    @Test fun straightLineIsNotScribble() {
        val line = (0..20).map { StrokePoint(it * 5f, 0f) }
        assertFalse(ScribbleDetector.isScribble(line))
    }

    @Test fun tightBackAndForthIsScribble() {
        // Zig-zag horizontally within a small box: many reversals, high ratio.
        val zig = ArrayList<StrokePoint>()
        var x = 0f
        var dir = 1f
        repeat(20) {
            zig.add(StrokePoint(x, 0f))
            x += dir * 20f
            if (x > 40f || x < 0f) { dir = -dir; x += dir * 20f }
        }
        assertTrue(ScribbleDetector.isScribble(zig))
    }

    @Test fun tooFewPointsIsNotScribble() {
        assertFalse(ScribbleDetector.isScribble(pts(0f to 0f, 1f to 1f)))
    }

    @Test fun gentleCurveIsNotScribble() {
        val curve = (0..20).map { StrokePoint(it * 5f, kotlin.math.sin(it / 3f) * 3f) }
        assertFalse(ScribbleDetector.isScribble(curve))
    }
}
