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

    /** Default pressure sensitivity (0 = uniform width, 1 = strong response). */
    const val DEFAULT_SENSITIVITY = 0.65f

    /**
     * @param base the pen's base width
     * @param pressure averaged 0f..1f over the segment
     * @param distance pixel distance between the two sampled points
     * @param sensitivity how much pressure affects width (0..1): 0 keeps a
     *   near-uniform line, 1 makes light/firm strokes very thin/thick
     * @return the stroke width to draw for this segment
     */
    fun of(
        base: Float,
        pressure: Float,
        distance: Float,
        sensitivity: Float = DEFAULT_SENSITIVITY,
    ): Float {
        val p = pressure.coerceIn(0f, 1f)
        val s = sensitivity.coerceIn(0f, 1f)
        // At s=0 the factor is 1 (uniform); at s=1 it ranges from ~0.15 (light)
        // to 1 (firm). Floor keeps the line from vanishing.
        val pressureFactor = (1f - s * (1f - p)).coerceAtLeast(0.15f)
        // Speed contributes 1.0 (slow) down to 0.55 (fast).
        val speed = (distance / FAST).coerceIn(0f, 1f)
        val speedFactor = 1f - 0.45f * speed
        return base * pressureFactor * speedFactor
    }

    /** Convenience: distance between two points. */
    fun distance(ax: Float, ay: Float, bx: Float, by: Float): Float =
        hypot(bx - ax, by - ay)
}
