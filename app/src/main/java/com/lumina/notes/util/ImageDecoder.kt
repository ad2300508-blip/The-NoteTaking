package com.lumina.notes.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * Decodes image files with downsampling so large camera photos (often 12 MP+)
 * don't blow up memory: a full-resolution ARGB_8888 bitmap of such a photo is
 * ~50 MB and decoding it directly can OOM. We cap the longest edge.
 */
object ImageDecoder {

    /** Decodes [path], scaled down so neither side exceeds [maxEdge]px, or null. */
    fun decodeSampled(path: String, maxEdge: Int = 2048): Bitmap? {
        if (path.isBlank()) return null
        return runCatching {
            // First pass: read only the bounds.
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            val opts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, maxEdge)
            }
            BitmapFactory.decodeFile(path, opts)
        }.getOrNull()
    }

    /**
     * Smallest power-of-two sample size so that both scaled dimensions are at
     * most [maxEdge]. (Doubles the sample while either edge still exceeds it.)
     */
    fun sampleSize(width: Int, height: Int, maxEdge: Int): Int {
        if (maxEdge <= 0) return 1
        var sample = 1
        while (width / sample > maxEdge || height / sample > maxEdge) {
            sample *= 2
        }
        return sample
    }
}
