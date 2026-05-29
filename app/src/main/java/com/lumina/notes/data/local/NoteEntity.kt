package com.lumina.notes.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * A single note. A note can hold both typed [body] text and freehand S Pen
 * [inkJson] (serialized strokes) on the same canvas, so the Galaxy Tab's
 * keyboard and stylus workflows live in one document.
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "title") val title: String = "",
    @ColumnInfo(name = "body") val body: String = "",
    /** Serialized ink strokes (see InkSerializer). Empty when there is no drawing. */
    @ColumnInfo(name = "ink_json") val inkJson: String = "",
    /** Index into the accent palette used to tint the note's card. */
    @ColumnInfo(name = "color_seed") val colorSeed: Int = 0,
    /** Ink canvas paper ruling (see PaperStyle). */
    @ColumnInfo(name = "paper_style", defaultValue = "0") val paperStyle: Int = 0,
    @ColumnInfo(name = "is_pinned") val isPinned: Boolean = false,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
) {
    /** A short preview used in the notes list. */
    val preview: String
        get() = body.trim().take(140)

    val hasInk: Boolean get() = inkJson.isNotBlank() && inkJson != "[]"
}
