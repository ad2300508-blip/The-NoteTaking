package com.lumina.notes.util

import com.lumina.notes.data.ink.PenTool
import com.lumina.notes.data.ink.Stroke
import com.lumina.notes.data.ink.StrokePoint
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InkExporterTest {

    private fun stroke(vararg pts: Pair<Float, Float>) = Stroke(
        points = pts.map { StrokePoint(it.first, it.second) },
        color = 0xFF000000L,
        baseWidth = 3f,
        tool = PenTool.PEN,
    )

    @Test fun boundsNullWhenNoStrokes() {
        assertNull(InkExporter.contentBounds(emptyList()))
    }

    @Test fun boundsNullWhenStrokesEmptyOfPoints() {
        val empty = Stroke(emptyList(), 0xFF000000L, 3f, PenTool.PEN)
        assertNull(InkExporter.contentBounds(listOf(empty)))
    }

    @Test fun boundsSpanAllStrokes() {
        val a = stroke(0f to 0f, 10f to 10f)
        val b = stroke(-5f to 20f, 30f to 7f)
        val bounds = InkExporter.contentBounds(listOf(a, b))!!
        assertArrayEquals(floatArrayOf(-5f, 0f, 30f, 20f), bounds, 0.001f)
    }
}
