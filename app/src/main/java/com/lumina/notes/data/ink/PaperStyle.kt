package com.lumina.notes.data.ink

/** Background ruling drawn behind the ink canvas. Order maps to the stored Int. */
enum class PaperStyle {
    PLAIN,
    DOTS,
    GRID,
    LINES;

    val label: String
        get() = when (this) {
            PLAIN -> "Liscio"
            DOTS -> "Punti"
            GRID -> "Griglia"
            LINES -> "Righe"
        }

    companion object {
        fun fromOrdinal(value: Int): PaperStyle =
            entries.getOrElse(value) { PLAIN }
    }
}
