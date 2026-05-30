package com.lumina.notes.data.ink

import kotlin.math.hypot

/**
 * Computes the on-screen width of a fountain-pen-like nib for a stroke
 * segment, modulated by both **pressure** and **speed**: pressing harder and
 * moving slowly gives a thicker line, quick strokes taper thin — like real
 * ink. Pure math so it can be unit tested.
 */
object NibWidth {

    /** Speed (in px) at which the line reaches its thinnest speed-factor. */
    private const val FAST = 60f

    /**
     * @param base the pen's base width
     * @param pressure averaged 0f..1f over the segment
     * @param distance pixel distance between the two sampled points
     * @return the stroke width to draw for this segment
     */
    fun of(base: Float, pressure: Float, distance: Float): Float {
        val p = pressure.coerceIn(0f, 1f)
        // Pressure contributes 35%..100% of base width.
        val pressureFactor = 0.35f + 0.65f * p
        // Speed contributes 1.0 (slow) down to 0.55 (fast).
        val speed = (distance / FAST).coerceIn(0f, 1f)
        val speedFactor = 1f - 0.45f * speed
        return base * pressureFactor * speedFactor
    }

    /** Convenience: distance between two points. */
    fun distance(ax: Float, ay: Float, bx: Float, by: Float): Float =
        hypot(bx - ax, by - ay)
}
