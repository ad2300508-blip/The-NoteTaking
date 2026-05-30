package com.lumina.notes.ui.ink

import androidx.compose.ui.geometry.Offset

/**
 * Maps between screen space and the canvas' logical drawing space so the ink
 * stays crisp under zoom/pan. A screen point maps to logical via:
 *
 *     logical = (screen - offset) / scale
 *
 * Kept as a small immutable value with pure math so it can be unit tested
 * without Compose.
 */
data class CanvasTransform(
    val scale: Float = 1f,
    val offset: Offset = Offset.Zero,
) {
    fun screenToCanvas(point: Offset): Offset = (point - offset) / scale

    fun canvasToScreen(point: Offset): Offset = point * scale + offset

    /**
     * Applies a pinch gesture: zooms by [zoom] around the screen-space
     * [centroid] and pans by [pan], clamping the scale to a sane range.
     */
    fun transform(centroid: Offset, zoom: Float, pan: Offset): CanvasTransform {
        val newScale = (scale * zoom).coerceIn(MIN_SCALE, MAX_SCALE)
        // Keep the logical point under the centroid fixed while zooming.
        val effectiveZoom = newScale / scale
        val newOffset = (offset - centroid) * effectiveZoom + centroid + pan
        return CanvasTransform(newScale, newOffset)
    }

    /**
     * Keeps the paged sheet in view: prevents scrolling above the top of the
     * page (y=0) and below the last page. [viewportHeight] and [sheetHeight]
     * are in screen pixels; the sheet is drawn at [scale].
     */
    fun clampVertical(viewportHeight: Float, sheetHeight: Float): CanvasTransform {
        val scaledSheet = sheetHeight * scale
        // offset.y is the screen position of the sheet's top edge.
        val maxY = 0f // don't pull the top below the viewport top
        val minY = if (scaledSheet <= viewportHeight) 0f else viewportHeight - scaledSheet
        val clampedY = offset.y.coerceIn(minY, maxY)
        return if (clampedY == offset.y) this
        else CanvasTransform(scale, Offset(offset.x, clampedY))
    }

    companion object {
        const val MIN_SCALE = 0.5f
        const val MAX_SCALE = 6f
    }
}
