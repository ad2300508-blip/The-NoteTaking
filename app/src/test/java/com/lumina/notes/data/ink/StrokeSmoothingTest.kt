package com.lumina.notes.data.ink

import org.junit.Assert.assertEquals
import org.junit.Test

class StrokeSmoothingTest {

    private fun p(x: Float, y: Float, pr: Float = 1f) = StrokePoint(x, y, pr)

    @Test fun tooFewPointsUnchanged() {
        val pts = listOf(p(0f, 0f), p(1f, 1f))
        assertEquals(pts, StrokeSmoothing.smooth(pts))
    }

    @Test fun endpointsPreserved() {
        val pts = listOf(p(0f, 0f), p(10f, 0f), p(20f, 0f))
        val out = StrokeSmoothing.smooth(pts)
        assertEquals(pts.first(), out.first())
        assertEquals(pts.last(), out.last())
    }

    @Test fun middleSpikeIsDampened() {
        // A jittery middle point should be pulled toward its neighbours.
        val pts = listOf(p(0f, 0f), p(10f, 100f), p(20f, 0f))
        val out = StrokeSmoothing.smooth(pts)
        assertEquals(10f, out[1].x, 0.001f)
        // average of 0,100,0 = 33.3 -> much less than the 100 spike
        assertEquals(33.333f, out[1].y, 0.01f)
    }

    @Test fun pressureIsPreservedPerPoint() {
        val pts = listOf(p(0f, 0f, 0.2f), p(1f, 1f, 0.7f), p(2f, 2f, 0.9f))
        val out = StrokeSmoothing.smooth(pts)
        assertEquals(0.7f, out[1].pressure, 0.001f)
    }

    @Test fun lengthIsPreserved() {
        val pts = (0..9).map { p(it.toFloat(), 0f) }
        assertEquals(pts.size, StrokeSmoothing.smooth(pts).size)
    }
}
