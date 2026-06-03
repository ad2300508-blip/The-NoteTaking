package com.lumina.notes.data.ink

import org.junit.Assert.assertEquals
import org.junit.Test

class StrokeTranslateTest {

    @Test fun translatesAllPoints() {
        val s = Stroke(
            points = listOf(StrokePoint(0f, 0f, 0.5f), StrokePoint(10f, 20f, 0.8f)),
            color = 0xFF000000L, baseWidth = 3f, tool = PenTool.PEN,
        )
        val moved = s.translated(5f, -3f)
        assertEquals(5f, moved.points[0].x, 0.001f)
        assertEquals(-3f, moved.points[0].y, 0.001f)
        assertEquals(15f, moved.points[1].x, 0.001f)
        assertEquals(17f, moved.points[1].y, 0.001f)
    }

    @Test fun preservesPressureColorWidthTool() {
        val s = Stroke(
            points = listOf(StrokePoint(1f, 1f, 0.7f)),
            color = 0xFF112233L, baseWidth = 5f, tool = PenTool.HIGHLIGHTER,
        )
        val moved = s.translated(2f, 2f)
        assertEquals(0.7f, moved.points[0].pressure, 0.001f)
        assertEquals(0xFF112233L, moved.color)
        assertEquals(5f, moved.baseWidth, 0.001f)
        assertEquals(PenTool.HIGHLIGHTER, moved.tool)
    }
}
