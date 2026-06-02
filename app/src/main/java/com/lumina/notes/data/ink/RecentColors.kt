package com.lumina.notes.data.ink

/**
 * A small most-recently-used ring of ink colors, so handwriting can switch
 * back to recent pens with one tap. Pure and tested; newest first, no
 * duplicates, capped at [capacity].
 */
class RecentColors(private val capacity: Int = 6, initial: List<Long> = emptyList()) {

    private val items = ArrayDeque<Long>()

    init {
        for (c in initial) add(c)
    }

    val colors: List<Long> get() = items.toList()

    /** Records [color] as the most recent, de-duplicating and capping size. */
    fun add(color: Long) {
        items.remove(color)
        items.addFirst(color)
        while (items.size > capacity) items.removeLast()
    }
}
