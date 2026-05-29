package com.lumina.notes.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Human-friendly relative time in Italian, e.g. "adesso", "5 min fa",
 * "3 h fa", "2 g fa", or an absolute date for anything older than a week.
 *
 * [nowMs] is injectable so the logic is deterministic under test.
 */
object RelativeTime {

    private const val MINUTE = 60_000L
    private const val HOUR = 60 * MINUTE
    private const val DAY = 24 * HOUR
    private const val WEEK = 7 * DAY

    fun format(timestampMs: Long, nowMs: Long = System.currentTimeMillis()): String {
        val diff = nowMs - timestampMs
        return when {
            diff < 0 -> "adesso"
            diff < MINUTE -> "adesso"
            diff < HOUR -> "${diff / MINUTE} min fa"
            diff < DAY -> "${diff / HOUR} h fa"
            diff < WEEK -> {
                val days = diff / DAY
                if (days == 1L) "ieri" else "$days g fa"
            }
            else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestampMs))
        }
    }
}
