package com.lumina.notes.data.local

/**
 * Stores a note's tags as a single comma-separated string. Pure and tested.
 * Tags are trimmed, de-duplicated case-insensitively (first spelling wins),
 * and free of empty entries and commas.
 */
object TagsCodec {

    fun decode(raw: String): List<String> =
        raw.split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    fun encode(tags: List<String>): String {
        val seen = LinkedHashMap<String, String>()
        for (t in tags) {
            val cleaned = t.trim().replace(",", " ").replace(Regex("\\s+"), " ")
            if (cleaned.isEmpty()) continue
            val key = cleaned.lowercase()
            if (!seen.containsKey(key)) seen[key] = cleaned
        }
        return seen.values.joinToString(",")
    }

    /** Adds [tag] to [raw], returning the new encoded string. */
    fun add(raw: String, tag: String): String = encode(decode(raw) + tag)

    /** Removes [tag] (case-insensitive) from [raw]. */
    fun remove(raw: String, tag: String): String =
        encode(decode(raw).filterNot { it.equals(tag, ignoreCase = true) })
}
