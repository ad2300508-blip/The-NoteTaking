package com.lumina.notes.data.repository

import com.lumina.notes.data.local.NoteDao
import com.lumina.notes.data.local.NoteEntity
import kotlinx.coroutines.flow.Flow

class NotesRepository(private val dao: NoteDao) {

    fun observeNotes(): Flow<List<NoteEntity>> = dao.observeAll()

    fun search(query: String): Flow<List<NoteEntity>> = dao.search(query.trim())

    fun observeNote(id: String): Flow<NoteEntity?> = dao.observeById(id)

    suspend fun getNote(id: String): NoteEntity? = dao.getById(id)

    suspend fun save(note: NoteEntity) =
        dao.upsert(note.copy(updatedAt = System.currentTimeMillis()))

    suspend fun delete(note: NoteEntity) = dao.delete(note)

    suspend fun deleteById(id: String) = dao.deleteById(id)

    suspend fun togglePin(note: NoteEntity) =
        dao.upsert(note.copy(isPinned = !note.isPinned))

    suspend fun toggleFavorite(note: NoteEntity) =
        dao.upsert(note.copy(isFavorite = !note.isFavorite))
}
