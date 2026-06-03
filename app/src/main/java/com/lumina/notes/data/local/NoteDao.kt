package com.lumina.notes.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    /** Pinned first, then most recently edited. */
    @Query(
        """
        SELECT * FROM notes
        ORDER BY is_pinned DESC, updated_at DESC
        """
    )
    fun observeAll(): Flow<List<NoteEntity>>

    @Query(
        """
        SELECT * FROM notes
        WHERE title LIKE '%' || :query || '%' COLLATE NOCASE
           OR body  LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY is_pinned DESC, updated_at DESC
        """
    )
    fun search(query: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): NoteEntity?

    @Upsert
    suspend fun upsert(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: String)
}
