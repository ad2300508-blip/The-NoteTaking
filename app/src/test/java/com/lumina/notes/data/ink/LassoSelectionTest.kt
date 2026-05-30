package com.lumina.notes.data.ink

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LassoSelectionTest {

    // A 10x10 square lasso around the origin region.
    private val square = listOf(
        StrokePoint(0f, 0f),
        StrokePoint(10f, 0f),
        StrokePoint(10f, 10f),
        StrokePoint(0f, 10f),
    )

    private fun stroke(vararg pts: Pair<Float, Float>) = Stroke(
        points = pts.map { StrokePoint(it.first, it.second) },
        color = 0xFF000000L, baseWidth = 3f, tool = PenTool.PEN,
    )

    @Test fun containsInsidePoint() {
        assertTrue(LassoSelection.contains(square, 5f, 5f))
    }

    @Test fun containsRejectsOutsidePoint() {
        assertFalse(LassoSelection.contains(square, 50f, 50f))
    }

    @Test fun strokeFullyInsideIsSelected() {
        val strokes = listOf(stroke(2f to 2f, 4f to 4f, 6f to 6f))
        assertEquals(listOf(0), LassoSelection.selectedIndices(strokes, square))
    }

    @Test fun strokeFullyOutsideIsNotSelected() {
        val strokes = listOf(stroke(20f to 20f, 30f to 30f))
        assertTrue(LassoSelection.selectedIndices(strokes, square).isEmpty())
    }

    @Test fun strokeMostlyInsidePassesThreshold() {
        // 3 of 4 points inside -> 0.75 >= 0.5
        val strokes = listOf(stroke(1f to 1f, 2f to 2f, 9f to 9f, 50f to 50f))
        assertEquals(listOf(0), LassoSelection.selectedIndices(strokes, square))
    }

    @Test fun strokeMostlyOutsideFailsThreshold() {
        val strokes = listOf(stroke(1f to 1f, 50f to 50f, 60f to 60f, 70f to 70f))
        assertTrue(LassoSelection.selectedIndices(strokes, square).isEmpty())
    }

    @Test fun degeneratePolygonSelectsNothing() {
        val strokes = listOf(stroke(5f to 5f))
        assertTrue(LassoSelection.selectedIndices(strokes, square.take(2)).isEmpty())
    }
}
