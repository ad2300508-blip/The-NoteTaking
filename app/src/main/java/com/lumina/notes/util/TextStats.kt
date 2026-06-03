package com.lumina.notes.util

import kotlin.math.ceil
import kotlin.math.max

/** Pure text statistics for the note editor. */
data class TextStats(
    val words: Int,
    val characters: Int,
    val readingMinutes: Int,
) {
    /** Compact Italian summary, e.g. "128 parole · 1 min". */
    fun summary(): String {
        val w = if (words == 1) "1 parola" else "$words parole"
        return "$w · $readingMinutes min"
    }
}

object TextStatsCalculator {

    /** Average adult silent reading speed (words per minute). */
    private const val WPM = 200

    fun of(text: String): TextStats {
        val trimmed = text.trim()
        val words = if (trimmed.isEmpty()) 0 else trimmed.split(Regex("\\s+")).size
        val minutes = if (words == 0) 0 else max(1, ceil(words.toDouble() / WPM).toInt())
        return TextStats(words = words, characters = text.length, readingMinutes = minutes)
    }
}
