package com.lumina.notes.data.ink

import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShapeRecognizerTest {

    private fun pt(x: Float, y: Float) = StrokePoint(x, y)

    @Test fun tooFewPointsIsNull() {
        assertNull(ShapeRecognizer.recognize(listOf(pt(0f, 0f), pt(1f, 1f))))
    }

    @Test fun tinyStrokeIsNull() {
        val pts = (0..10).map { pt(it.toFloat(), 0f) } // span 10px < 24
        assertNull(ShapeRecognizer.recognize(pts))
    }

    @Test fun straightLineIsLine() {
        val pts = (0..20).map { pt(it * 10f, (it % 2) * 0.5f) } // long, very thin
        val shape = ShapeRecognizer.recognize(pts)
        assertTrue("expected Line but got $shape", shape is RecognizedShape.Line)
    }

    @Test fun roughRectangleIsRectangle() {
        // Trace a 100x60 box perimeter.
        val pts = ArrayList<StrokePoint>()
        for (x in 0..100 step 10) pts.add(pt(x.toFloat(), 0f))
        for (y in 0..60 step 10) pts.add(pt(100f, y.toFloat()))
        for (x in 100 downTo 0 step 10) pts.add(pt(x.toFloat(), 60f))
        for (y in 60 downTo 0 step 10) pts.add(pt(0f, y.toFloat()))
        val shape = ShapeRecognizer.recognize(pts)
        assertTrue("expected Rectangle but got $shape", shape is RecognizedShape.Rectangle)
    }

    @Test fun roughCircleIsEllipse() {
        val cx = 50f; val cy = 50f; val r = 40f
        val pts = (0..36).map {
            val a = Math.toRadians(it * 10.0)
            pt((cx + r * Math.cos(a)).toFloat(), (cy + r * Math.sin(a)).toFloat())
        }
        val shape = ShapeRecognizer.recognize(pts)
        assertTrue("expected Ellipse but got $shape", shape is RecognizedShape.Ellipse)
    }

    @Test fun ellipsePointsRoughlyOnCurve() {
        val e = RecognizedShape.Ellipse(cx = 0f, cy = 0f, rx = 10f, ry = 5f)
        val pts = ShapeRecognizer.ellipseToPoints(e, steps = 4)
        // First point at angle 0 -> (rx, 0)
        assertTrue(kotlin.math.abs(pts.first().x - 10f) < 0.001f)
    }
}
