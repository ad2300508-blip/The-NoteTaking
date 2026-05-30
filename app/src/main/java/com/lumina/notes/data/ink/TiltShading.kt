package com.lumina.notes.data.ink

import kotlin.math.PI

/**
 * Maps S Pen **tilt** to a chisel-like width multiplier, so a more reclined pen
 * lays down a broader stroke (like the broad edge of a highlighter or a
 * calligraphy nib) while an upright pen stays fine. Pure math, unit tested.
 *
 * [tiltRadians] is the angle from vertical, as reported by
 * MotionEvent.AXIS_TILT (0 = perpendicular to the screen, ~PI/2 = flat).
 */
object TiltShading {

    /** Tilt at/above this (radians) reaches the maximum broadening. */
    private const val MAX_TILT = (PI / 3).toFloat() // 60°

    /**
     * @param maxBroaden the largest multiplier at full tilt (e.g. 2.5f)
     * @return a width multiplier in 1f..[maxBroaden]
     */
    fun widthMultiplier(tiltRadians: Float, maxBroaden: Float = 2.5f): Float {
        if (tiltRadians.isNaN() || tiltRadians <= 0f) return 1f
        val t = (tiltRadians / MAX_TILT).coerceIn(0f, 1f)
        return 1f + (maxBroaden - 1f) * t
    }
}
