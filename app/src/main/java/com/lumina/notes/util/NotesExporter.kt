package com.lumina.notes.util

import com.lumina.notes.data.local.NoteEntity
import com.lumina.notes.data.local.TagsCodec
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Renders the whole library to a single human-readable Markdown document for
 * backup/sharing. Pure (no Android deps beyond formatting) so it's testable.
 */
object NotesExporter {

    private fun stamp(ms: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(ms))

    fun toMarkdown(notes: List<NoteEntity>): String {
        if (notes.isEmpty()) return "# Lumina\n\n_Nessuna nota._\n"
        val sb = StringBuilder()
        sb.append("# Lumina — Esportazione note\n\n")
        sb.append("${notes.size} note · esportate il ${stamp(System.currentTimeMillis())}\n\n")
        for (note in notes) {
            val title = note.title.ifBlank { "(senza titolo)" }
            sb.append("## ").append(title).append('\n')

            val meta = buildList {
                if (note.isPinned) add("📌 fissata")
                if (note.isFavorite) add("⭐ preferita")
                if (note.hasInk) add("✍️ disegno")
                val tags = TagsCodec.decode(note.tags)
                if (tags.isNotEmpty()) add(tags.joinToString(" ") { "#$it" })
            }
            if (meta.isNotEmpty()) sb.append('_').append(meta.joinToString(" · ")).append("_\n")
            sb.append("\n").append("Modificata: ").append(stamp(note.updatedAt)).append("\n\n")

            if (note.body.isNotBlank()) sb.append(note.body.trim()).append("\n\n")
            sb.append("---\n\n")
        }
        return sb.toString()
    }

    /** Default file name for the exported document. */
    fun fileName(now: Long = System.currentTimeMillis()): String =
        "lumina-note-" + SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault()).format(Date(now)) + ".md"
}
