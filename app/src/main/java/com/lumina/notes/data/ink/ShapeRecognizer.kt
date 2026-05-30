package com.lumina.notes.data.ink

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/** The idealized shape a freehand stroke was recognized as. */
sealed interface RecognizedShape {
    data class Line(val x1: Float, val y1: Float, val x2: Float, val y2: Float) : RecognizedShape
    data class Rectangle(val left: Float, val top: Float, val right: Float, val bottom: Float) :
        RecognizedShape
    data class Ellipse(val cx: Float, val cy: Float, val rx: Float, val ry: Float) :
        RecognizedShape
}

/**
 * Recognizes a hand-drawn stroke as a straight line, rectangle or ellipse and
 * returns its cleaned-up geometry — the classic "draw a rough shape, get a
 * neat one" S Pen helper. Pure math, fully unit tested.
 */
object ShapeRecognizer {

    fun recognize(points: List<StrokePoint>): RecognizedShape? {
        if (points.size < 4) return null

        val bx = bounds(points)
        val left = bx[0]; val top = bx[1]; val right = bx[2]; val bottom = bx[3]
        val w = right - left
        val h = bottom - top
        val diag = hypot(w, h)
        if (diag < 24f) return null // too small to be intentional

        val first = points.first()
        val last = points.last()
        val endGap = hypot(last.x - first.x, last.y - first.y)
        val closed = endGap < 0.25f * diag

        // --- Straight line: thin bounding box and open ends. ---
        val thin = min(w, h) < 0.18f * max(w, h)
        if (!closed && thin) {
            return RecognizedShape.Line(first.x, first.y, last.x, last.y)
        }

        if (!closed) return null

        // Closed shape: decide rectangle vs ellipse by how much of the
        // bounding-box area the path fills. A circle fills ~PI/4 (~0.785),
        // a rectangle fills ~1.0.
        val area = polygonArea(points)
        val boxArea = (w * h).coerceAtLeast(1f)
        val fill = (area / boxArea).coerceIn(0f, 1.2f)

        return if (fill < 0.85f) {
            RecognizedShape.Ellipse(
                cx = (left + right) / 2f,
                cy = (top + bottom) / 2f,
                rx = w / 2f,
                ry = h / 2f,
            )
        } else {
            RecognizedShape.Rectangle(left, top, right, bottom)
        }
    }

    /** Samples an ellipse outline into stroke points (for re-drawing). */
    fun ellipseToPoints(e: RecognizedShape.Ellipse, steps: Int = 48): List<StrokePoint> {
        val out = ArrayList<StrokePoint>(steps + 1)
        for (i in 0..steps) {
            val a = (2 * PI * i / steps)
            out.add(
                StrokePoint(
                    x = (e.cx + e.rx * kotlin.math.cos(a)).toFloat(),
                    y = (e.cy + e.ry * kotlin.math.sin(a)).toFloat(),
                )
            )
        }
        return out
    }

    /** Rectangle outline as a closed point list. */
    fun rectToPoints(r: RecognizedShape.Rectangle): List<StrokePoint> = listOf(
        StrokePoint(r.left, r.top), StrokePoint(r.right, r.top),
        StrokePoint(r.right, r.bottom), StrokePoint(r.left, r.bottom),
        StrokePoint(r.left, r.top),
    )

    private fun bounds(points: List<StrokePoint>): FloatArray {
        var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE; var maxY = -Float.MAX_VALUE
        for (p in points) {
            if (p.x < minX) minX = p.x; if (p.x > maxX) maxX = p.x
            if (p.y < minY) minY = p.y; if (p.y > maxY) maxY = p.y
        }
        return floatArrayOf(minX, minY, maxX, maxY)
    }

    /** Shoelace formula (absolute area). */
    private fun polygonArea(points: List<StrokePoint>): Float {
        var sum = 0f
        for (i in points.indices) {
            val a = points[i]
            val b = points[(i + 1) % points.size]
            sum += a.x * b.y - b.x * a.y
        }
        return abs(sum) / 2f
    }
}
