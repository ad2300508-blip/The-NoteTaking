package com.lumina.notes.util

/**
 * Builds a short preview snippet centered on the first case-insensitive match
 * of [query] within [text], with ellipses when truncated. Pure and tested.
 */
object SearchSnippet {

    fun of(text: String, query: String, radius: Int = 40): String {
        val trimmed = text.trim()
        val q = query.trim()
        if (q.isEmpty() || trimmed.isEmpty()) return trimmed.take(radius * 2)

        val idx = trimmed.indexOf(q, ignoreCase = true)
        if (idx < 0) return trimmed.take(radius * 2)

        val start = (idx - radius).coerceAtLeast(0)
        val end = (idx + q.length + radius).coerceAtMost(trimmed.length)
        val core = trimmed.substring(start, end).replace('\n', ' ')
        val prefix = if (start > 0) "…" else ""
        val suffix = if (end < trimmed.length) "…" else ""
        return "$prefix$core$suffix"
    }
}
