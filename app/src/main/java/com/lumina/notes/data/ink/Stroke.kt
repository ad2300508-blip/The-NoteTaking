package com.lumina.notes.data.ink

import androidx.compose.ui.geometry.Offset

/** One sampled point of an S Pen stroke. [pressure] is normalized 0f..1f. */
data class StrokePoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1f,
) {
    val offset: Offset get() = Offset(x, y)
}

/** The drawing tool that produced a stroke. */
enum class PenTool {
    PEN,
    HIGHLIGHTER,
    ERASER;
}

/**
 * A complete freehand stroke. Coordinates are stored in the canvas' own
 * logical space (independent of zoom/scroll) so drawings stay crisp.
 */
data class Stroke(
    val points: List<StrokePoint>,
    val color: Long,
    val baseWidth: Float,
    val tool: PenTool,
) {
    val isEmpty: Boolean get() = points.isEmpty()

    /** Axis-aligned bounds, used for fast hit-testing by the eraser. */
    fun bounds(): FloatArray {
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE
        for (p in points) {
            if (p.x < minX) minX = p.x
            if (p.y < minY) minY = p.y
            if (p.x > maxX) maxX = p.x
            if (p.y > maxY) maxY = p.y
        }
        return floatArrayOf(minX, minY, maxX, maxY)
    }
}
