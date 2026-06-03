package com.lumina.notes.data.ink

/**
 * Light moving-average smoothing of a freehand stroke. Reduces the S Pen's
 * sampling jitter so handwriting looks cleaner, while preserving the first and
 * last points (so the stroke still starts/ends exactly where the pen did) and
 * each point's pressure. Pure, so it can be unit tested.
 */
object StrokeSmoothing {

    /**
     * @param points the raw sampled points
     * @param window neighbours on each side to average (1 = 3-point window)
     */
    fun smooth(points: List<StrokePoint>, window: Int = 1): List<StrokePoint> {
        if (points.size < 3 || window < 1) return points
        val out = ArrayList<StrokePoint>(points.size)
        out.add(points.first())
        for (i in 1 until points.size - 1) {
            val lo = (i - window).coerceAtLeast(0)
            val hi = (i + window).coerceAtMost(points.size - 1)
            var sx = 0f
            var sy = 0f
            var n = 0
            for (k in lo..hi) {
                sx += points[k].x
                sy += points[k].y
                n++
            }
            out.add(
                StrokePoint(
                    x = sx / n,
                    y = sy / n,
                    pressure = points[i].pressure,
                )
            )
        }
        out.add(points.last())
        return out
    }
}
