package com.lumina.notes.data.ink

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private const val EPS = 0.001f

class StrokeTest {

    @Test
    fun bounds_computesTightAxisAlignedBox() {
        val stroke = Stroke(
            points = listOf(
                StrokePoint(10f, 20f),
                StrokePoint(40f, 5f),
                StrokePoint(25f, 80f),
            ),
            color = 0xFF000000L,
            baseWidth = 4f,
            tool = PenTool.PEN,
        )

        val b = stroke.bounds()
        assertEquals(10f, b[0], EPS) // minX
        assertEquals(5f, b[1], EPS)  // minY
        assertEquals(40f, b[2], EPS) // maxX
        assertEquals(80f, b[3], EPS) // maxY
    }

    @Test
    fun emptyStroke_isReportedEmpty() {
        val stroke = Stroke(emptyList(), 0xFFFFFFFFL, 2f, PenTool.HIGHLIGHTER)
        assertTrue(stroke.isEmpty)
    }
}
