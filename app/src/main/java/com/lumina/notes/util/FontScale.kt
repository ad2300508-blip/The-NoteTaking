package com.lumina.notes.util

/**
 * Clamps and snaps the user's preferred text scale for the note editor.
 * Pure and tested so the UI and persistence agree on valid values.
 */
object FontScale {
    const val MIN = 0.8f
    const val MAX = 1.6f
    const val DEFAULT = 1.0f

    /** Coerces [value] into [MIN]..[MAX]; NaN/invalid falls back to [DEFAULT]. */
    fun sanitize(value: Float): Float =
        if (value.isNaN() || value <= 0f) DEFAULT else value.coerceIn(MIN, MAX)

    /** A short label like "100%" for display next to the slider. */
    fun label(value: Float): String = "${(sanitize(value) * 100).toInt()}%"
}
