package com.lumina.notes.util

import com.lumina.notes.data.local.NoteEntity
import com.lumina.notes.data.local.TagsCodec

/** Aggregate counts about the whole note library, for a settings summary. */
data class LibraryStats(
    val total: Int,
    val withInk: Int,
    val favorites: Int,
    val pinned: Int,
    val distinctTags: Int,
) {
    fun summary(): String = buildString {
        append(if (total == 1) "1 nota" else "$total note")
        if (withInk > 0) append(" · $withInk con disegni")
        if (distinctTags > 0) append(" · $distinctTags tag")
    }
}

object LibraryStatsCalculator {
    fun of(notes: List<NoteEntity>): LibraryStats {
        val tags = notes
            .flatMap { TagsCodec.decode(it.tags) }
            .distinctBy { it.lowercase() }
        return LibraryStats(
            total = notes.size,
            withInk = notes.count { it.hasInk },
            favorites = notes.count { it.isFavorite },
            pinned = notes.count { it.isPinned },
            distinctTags = tags.size,
        )
    }
}
