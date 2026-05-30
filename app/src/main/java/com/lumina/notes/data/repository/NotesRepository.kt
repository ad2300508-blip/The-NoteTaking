package com.lumina.notes.data.repository

import com.lumina.notes.data.local.NoteDao
import com.lumina.notes.data.local.NoteEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class NotesRepository(private val dao: NoteDao) {

    fun observeNotes(): Flow<List<NoteEntity>> = dao.observeAll()

    fun search(query: String): Flow<List<NoteEntity>> = dao.search(query.trim())

    fun observeNote(id: String): Flow<NoteEntity?> = dao.observeById(id)

    suspend fun getNote(id: String): NoteEntity? = dao.getById(id)

    suspend fun save(note: NoteEntity) =
        dao.upsert(note.copy(updatedAt = System.currentTimeMillis()))

    /** Re-inserts a note exactly as-is (e.g. undoing a delete) without bumping timestamps. */
    suspend fun restore(note: NoteEntity) = dao.upsert(note)

    suspend fun delete(note: NoteEntity) = dao.delete(note)

    suspend fun deleteById(id: String) = dao.deleteById(id)

    suspend fun togglePin(note: NoteEntity) =
        dao.upsert(note.copy(isPinned = !note.isPinned))

    suspend fun toggleFavorite(note: NoteEntity) =
        dao.upsert(note.copy(isFavorite = !note.isFavorite))

    /** Creates an independent copy of [note] (fresh id, not pinned) and returns it. */
    suspend fun duplicate(note: NoteEntity): NoteEntity {
        val now = System.currentTimeMillis()
        val copy = note.copy(
            id = UUID.randomUUID().toString(),
            title = duplicateTitle(note.title),
            isPinned = false,
            createdAt = now,
            updatedAt = now,
        )
        dao.upsert(copy)
        return copy
    }

    companion object {
        /** Title for a duplicated note; blank stays blank. Pure, for testing. */
        fun duplicateTitle(title: String): String =
            if (title.isBlank()) "" else "$title (copia)"
    }
}
