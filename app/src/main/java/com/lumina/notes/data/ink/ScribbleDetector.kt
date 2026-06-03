package com.lumina.notes.data.ink

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

/**
 * Detects a "scribble to erase" gesture: a dense back-and-forth scrawl in a
 * small area (like crossing something out by hand). Pure geometry over a
 * stroke's points, so it's unit tested without any input plumbing.
 *
 * Heuristic: the stroke must have enough length packed into a small bounding
 * box (high length/diagonal ratio) and reverse direction several times.
 */
object ScribbleDetector {

    fun isScribble(
        points: List<StrokePoint>,
        minDirectionChanges: Int = 3,
        minLengthRatio: Float = 3.5f,
    ): Boolean {
        if (points.size < 5) return false

        var pathLength = 0f
        var reversals = 0
        var prevAngle = Float.NaN

        for (i in 1 until points.size) {
            val dx = points[i].x - points[i - 1].x
            val dy = points[i].y - points[i - 1].y
            val seg = hypot(dx, dy)
            if (seg < 0.5f) continue
            pathLength += seg
            val angle = atan2(dy, dx)
            if (!prevAngle.isNaN()) {
                var diff = abs(angle - prevAngle)
                if (diff > Math.PI) diff = (2 * Math.PI - diff).toFloat()
                // A near-U-turn (> ~120°) counts as a direction reversal.
                if (diff > 2.094f) reversals++
            }
            prevAngle = angle
        }

        val b = boundsDiagonal(points)
        if (b < 1f) return false
        val ratio = pathLength / b
        return reversals >= minDirectionChanges && ratio >= minLengthRatio
    }

    private fun boundsDiagonal(points: List<StrokePoint>): Float {
        var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE; var maxY = -Float.MAX_VALUE
        for (p in points) {
            if (p.x < minX) minX = p.x; if (p.x > maxX) maxX = p.x
            if (p.y < minY) minY = p.y; if (p.y > maxY) maxY = p.y
        }
        return hypot(maxX - minX, maxY - minY)
    }
}
