package com.lumina.notes.data.ink

/**
 * Geometry for the lasso (free-form) selection of ink strokes. Pure math so it
 * is unit tested independently of Compose. A stroke is considered selected when
 * a sufficient fraction of its points fall inside the lasso polygon.
 */
object LassoSelection {

    /** Point-in-polygon via the ray-casting (even-odd) rule. */
    fun contains(polygon: List<StrokePoint>, x: Float, y: Float): Boolean {
        if (polygon.size < 3) return false
        var inside = false
        var j = polygon.size - 1
        for (i in polygon.indices) {
            val xi = polygon[i].x; val yi = polygon[i].y
            val xj = polygon[j].x; val yj = polygon[j].y
            val intersects = (yi > y) != (yj > y) &&
                x < (xj - xi) * (y - yi) / ((yj - yi).takeIf { it != 0f } ?: 1e-6f) + xi
            if (intersects) inside = !inside
            j = i
        }
        return inside
    }

    /**
     * Indices of [strokes] selected by [polygon]: a stroke qualifies when at
     * least [threshold] (0..1) of its points lie inside the lasso.
     */
    fun selectedIndices(
        strokes: List<Stroke>,
        polygon: List<StrokePoint>,
        threshold: Float = 0.5f,
    ): List<Int> {
        if (polygon.size < 3) return emptyList()
        val result = ArrayList<Int>()
        for ((index, stroke) in strokes.withIndex()) {
            if (stroke.points.isEmpty()) continue
            val inside = stroke.points.count { contains(polygon, it.x, it.y) }
            if (inside.toFloat() / stroke.points.size >= threshold) result.add(index)
        }
        return result
    }
}
