package com.lumina.notes.data.ink

import kotlin.math.hypot

/**
 * Detects a quick double-tap of the S Pen tip: two short taps close together
 * in time and space. Used as a shortcut to toggle the eraser while writing,
 * without reaching for the toolbar. Pure and stateful-but-testable: feed it
 * each tap (a pen-down/up that didn't draw a real stroke).
 */
class DoubleTapDetector(
    private val timeoutMs: Long = 350,
    private val slopPx: Float = 48f,
) {
    private var lastTapTime = 0L
    private var lastX = 0f
    private var lastY = 0f

    /**
     * Registers a tap at ([x], [y]) at [now] (ms). Returns true when this tap
     * completes a double-tap (and resets so a triple-tap isn't two doubles).
     */
    fun onTap(x: Float, y: Float, now: Long): Boolean {
        val dt = now - lastTapTime
        val close = hypot(x - lastX, y - lastY) <= slopPx
        val isDouble = lastTapTime != 0L && dt in 1..timeoutMs && close
        if (isDouble) {
            reset()
            return true
        }
        lastTapTime = now
        lastX = x
        lastY = y
        return false
    }

    fun reset() {
        lastTapTime = 0L
        lastX = 0f
        lastY = 0f
    }
}
