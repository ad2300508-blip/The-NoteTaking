package com.lumina.notes.data.ink

import kotlin.math.ceil

/**
 * Computes how tall the notebook canvas should be so it grows downward like
 * real pages: the sheet is a whole number of [pageHeight] pages, always at
 * least [minPages], and gains an extra blank page once ink reaches within
 * [growMargin] of the current bottom. Pure, so it's unit tested.
 */
object PageMetrics {

    /**
     * @param contentBottom the largest y-coordinate of any ink (0 if empty)
     * @param pageHeight the height of one page in canvas units
     * @param minPages minimum number of pages to always show
     * @param growMargin add a page when content is within this of the bottom
     * @return the canvas height in canvas units (a multiple of [pageHeight])
     */
    fun canvasHeight(
        contentBottom: Float,
        pageHeight: Float,
        minPages: Int = 1,
        growMargin: Float = 0f,
    ): Float {
        require(pageHeight > 0f) { "pageHeight must be > 0" }
        val needed = ceil((contentBottom + growMargin) / pageHeight).toInt()
        val pages = maxOf(minPages, needed, 1)
        return pages * pageHeight
    }

    /** Number of pages currently represented by [canvasHeight]. */
    fun pageCount(canvasHeight: Float, pageHeight: Float): Int {
        if (pageHeight <= 0f) return 1
        return maxOf(1, ceil(canvasHeight / pageHeight).toInt())
    }
}
